package me.saharnooby.qoi.benchmark;

import lombok.NonNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author saharNooby
 * @since 15:10 24.12.2021
 */
public final class FormatComparisonBenchmark {

	public static volatile Object blackHole;

	private enum Mode {

		DECODE, ENCODE

	}

	private enum Format {

		QOI, PNG

	}

	public static void main(String[] args) throws Exception {
		File dir = new File(args[0]);

		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("Invalid directory");
		}

		// Settings
		int warmUpSeconds = 5;
		int runSeconds = 5;
		int maxImages = 50;

		// ---

		// Disable cache so file IO does not skew the results
		ImageIO.setUseCache(false);

		List<File> files = findFiles(dir);

		if (files.size() > maxImages) {
			files.sort(Comparator.naturalOrder());

			Collections.shuffle(files, new Random(1));

			System.out.println("Using " + maxImages + " images out of " + files.size());

			files = files.subList(0, maxImages);
		}

		List<Image> images = new ArrayList<>();

		for (File file : files) {
			images.add(new Image(file));
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream(16 * 1024 * 1024);

		int total = images.size() * Mode.values().length * Format.values().length;

		int done = 0;

		for (Image image : images) {
			for (boolean encode : new boolean[] {false, true}) {
				for (Format format : Format.values()) {
					// Display progress
					System.out.println("[" + ++done + "/" + total + "] " + image.file.getName() + ", " + encode + ", " + format);

					Result result = image.results.computeIfAbsent(format, k -> new Result());

					int encodedSize = 0;

					List<Double> iterationDelays = new ArrayList<>();

					long start = System.nanoTime();

					while (true) {
						out.reset();

						long iterationStart = System.nanoTime();

						if (encode) {
							if (!ImageIO.write(image.image, format.name(), out)) {
								throw new IllegalStateException("Failed to write " + format);
							}

							blackHole = encodedSize = out.size();
						} else {
							blackHole = Objects.requireNonNull(ImageIO.read(new ByteArrayInputStream(image.encoded.get(format))));
						}

						long iterationDelay = System.nanoTime() - iterationStart;

						long totalDelay = System.nanoTime() - start;

						if (totalDelay > TimeUnit.SECONDS.toNanos(warmUpSeconds + runSeconds)) {
							break;
						}

						if (totalDelay > TimeUnit.SECONDS.toNanos(warmUpSeconds)) {
							iterationDelays.add(iterationDelay / 1_000_000D);
						}
					}

					double averageDelay = iterationDelays.stream().mapToDouble(Double::doubleValue).average().orElse(0);

					if (encode) {
						result.encode = averageDelay;
						result.size = encodedSize;
					} else {
						result.decode = averageDelay;
					}
				}
			}
		}

		// Print results
		System.out.println();
		System.out.println(System.getProperty("java.runtime.name") + " " + System.getProperty("java.runtime.version"));
		System.out.println("Warm up: " + warmUpSeconds + " seconds, run: " + runSeconds + " seconds");
		System.out.println(images.size() + " files total");

		System.out.println();
		System.out.println("Average over all files");
		printTable(average(images));

		for (Image image : images) {
			System.out.println();
			System.out.println(image.file.getName() + ", " + image.image.getWidth() + " x " + image.image.getHeight());

			printTable(image.results);
		}
	}

	private static Map<Format, Result> average(@NonNull Collection<Image> images) {
		if (images.isEmpty()) {
			return Collections.emptyMap();
		}

		Set<Format> keys = EnumSet.noneOf(Format.class);

		for (Image image : images) {
			if (keys.isEmpty()) {
				keys.addAll(image.results.keySet());
			} else {
				keys.retainAll(image.results.keySet());
			}
		}

		Map<Format, Result> average = new EnumMap<>(Format.class);

		for (Format key : keys) {
			average.put(key, new Result());
		}

		for (Image image : images) {
			for (Format key : keys) {
				Result imageRes = image.results.get(key);

				Result sum = average.get(key);

				sum.decode += imageRes.decode;
				sum.encode += imageRes.encode;
				sum.size += imageRes.size;
			}
		}

		for (Result result : average.values()) {
			result.decode /= images.size();
			result.encode /= images.size();
			result.size /= images.size();
		}

		return average;
	}

	private static void printTable(@NonNull Map<Format, Result> results) {
		if (results.isEmpty()) {
			System.out.println("No results");

			return;
		}

		int width = 14;

		System.out.println(pad("Format", 0, 8) +
				pad("Decode", width * 2, 0) +
				pad("Encode", width * 2, 0) +
				pad("Size", width * 2, 0));

		for (Map.Entry<Format, Result> e : results.entrySet()) {
			Result result = e.getValue();

			Result qoi = results.get(Format.QOI);

			System.out.println(pad(e.getKey().name(), 0, 8) +
					pad(formatMs(result.decode), width, 0) +
					pad(compare(result.decode, qoi.decode), width, 0) +
					pad(formatMs(result.encode), width, 0) +
					pad(compare(result.encode, qoi.encode), width, 0) +
					pad(result.size / 1024 + " KB", width, 0) +
					pad(compare(result.size, qoi.size), width, 0));
		}
	}

	private static String compare(double value, double comparedTo) {
		if (value == comparedTo) {
			return "";
		}

		double percent = (value - comparedTo) / comparedTo * 100;

		return (percent > 0 ? "+" : "") + (int) percent + "%";
	}

	private static String formatMs(double v) {
		return String.format(Locale.ROOT, "%.3f ms", v);
	}

	@SuppressWarnings("StringConcatenationInLoop")
	private static String pad(@NonNull String s, int left, int right) {
		while (s.length() < left) {
			s = " " + s;
		}

		while (s.length() < right) {
			s = s + " ";
		}

		return s;
	}

	private static List<File> findFiles(@NonNull File root) {
		List<File> result = new ArrayList<>();

		LinkedList<File> stack = new LinkedList<>();

		stack.push(root);

		while (!stack.isEmpty()) {
			File dir = stack.pop();

			for (File file : Objects.requireNonNull(dir.listFiles())) {
				if (file.isDirectory()) {
					stack.push(file);
				} else {
					if (file.getName().endsWith(".txt")) {
						continue;
					}

					result.add(file);
				}
			}
		}

		return result;
	}

	private static final class Result {

		double decode;
		double encode;
		long size;

	}

	private static final class Image {

		final File file;
		final BufferedImage image;
		final Map<Format, byte[]> encoded = new EnumMap<>(Format.class);
		final Map<Format, Result> results = new EnumMap<>(Format.class);

		public Image(@NonNull File file) throws Exception {
			this.file = file;

			BufferedImage image = Objects.requireNonNull(ImageIO.read(file), "Invalid image in " + file);

			if (image.getColorModel() instanceof IndexColorModel) {
				// QOI can't write indexed images directly, we need to convert it to normal type
				int type = image.getTransparency() == Transparency.OPAQUE ?
						BufferedImage.TYPE_3BYTE_BGR :
						BufferedImage.TYPE_4BYTE_ABGR;

				BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), type);
				Graphics g = copy.getGraphics();
				g.drawImage(image, 0, 0, null);
				g.dispose();
				image = copy;
			}

			this.image = image;

			ByteArrayOutputStream out = new ByteArrayOutputStream(16 * 1024 * 1024);

			for (Format format : Format.values()) {
				out.reset();

				if (!ImageIO.write(this.image, format.name(), out)) {
					throw new IllegalStateException("Failed to encode " + file + " as " + format);
				}

				this.encoded.put(format, out.toByteArray());
			}
		}

	}

}
