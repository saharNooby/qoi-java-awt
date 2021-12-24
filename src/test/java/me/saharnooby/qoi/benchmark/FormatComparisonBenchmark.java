package me.saharnooby.qoi.benchmark;

import lombok.NonNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
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

		QOI, PNG, JPG, BMP

	}

	public static void main(String[] args) throws Exception {
		File dir = new File(args[0]);

		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("Invalid directory");
		}

		// Settings
		int warmUpSec = 5;
		int runSec = 5;

		// ---

		// Disable cache so file IO does not skew the results
		ImageIO.setUseCache(false);

		List<Image> images = new ArrayList<>();

		for (File file : Objects.requireNonNull(dir.listFiles())) {
			images.add(new Image(file));
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream(16 * 1024 * 1024);

		for (Image image : images) {
			System.out.println(image.file.getName() + ", " + image.image.getWidth() + " x " + image.image.getHeight());

			for (Mode mode : Mode.values()) {
				System.out.println("\t" + mode.name().toLowerCase());

				for (Format format : Format.values()) {
					if (!image.isFormatSupported(format)) {
						continue;
					}

					int encodedSize = 0;

					List<Double> delays = new ArrayList<>();

					long start = System.nanoTime();

					while (true) {
						out.reset();

						long nano = System.nanoTime();

						if (mode == Mode.DECODE) {
							blackHole = Objects.requireNonNull(ImageIO.read(new ByteArrayInputStream(image.encoded.get(format))));
						} else {
							if (!ImageIO.write(image.image, format.name(), out)) {
								throw new IllegalStateException("Failed to write " + format);
							}

							blackHole = encodedSize = out.size();
						}

						double delay = (System.nanoTime() - nano) / 1_000_000D;

						long totalDelay = System.nanoTime() - start;

						if (totalDelay > TimeUnit.SECONDS.toNanos(warmUpSec + runSec)) {
							break;
						}

						if (totalDelay > TimeUnit.SECONDS.toNanos(warmUpSec)) {
							delays.add(delay);
						}
					}

					double ms = delays.stream().mapToDouble(Double::doubleValue).average().orElse(0);

					String timeFormatted = String.format(Locale.ROOT, "%.3f ms", ms);

					System.out.println("\t\t" + format + ": " + timeFormatted + (encodedSize > 0 ? ", " + encodedSize / 1024 + " KB" : ""));
				}
			}
		}
	}

	private static final class Image {

		final File file;
		final BufferedImage image;
		final Map<Format, byte[]> encoded = new EnumMap<>(Format.class);

		public Image(@NonNull File file) throws Exception {
			this.file = file;
			this.image = ImageIO.read(file);

			ByteArrayOutputStream out = new ByteArrayOutputStream(16 * 1024 * 1024);

			for (Format format : Format.values()) {
				if (isFormatSupported(format)) {
					out.reset();

					ImageIO.write(this.image, format.name(), out);

					this.encoded.put(format, out.toByteArray());
				}
			}
		}

		public boolean isFormatSupported(@NonNull Format format) {
			if (format == Format.JPG || format == Format.BMP) {
				return this.image.getTransparency() == Transparency.OPAQUE;
			}

			return true;
		}

	}

}
