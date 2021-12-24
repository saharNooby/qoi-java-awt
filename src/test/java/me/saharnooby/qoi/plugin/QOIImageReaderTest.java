package me.saharnooby.qoi.plugin;

import lombok.NonNull;
import me.saharnooby.qoi.QOIUtil;
import me.saharnooby.qoi.QOIUtilAWT;
import me.saharnooby.qoi.util.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;

/**
 * @author saharNooby
 * @since 16:01 23.12.2021
 */
class QOIImageReaderTest {

	@Test
	void testDefaultParams() throws Exception {
		test(ImageIO::read);

		test(in -> getReader(in).read(0, null));
	}

	@Test
	void testCustomSourceRegionWithSubsampling() throws Exception {
		test(in -> {
			ImageReadParam param = new ImageReadParam();
			// Out of bounds on purpose
			param.setSourceRegion(new Rectangle(99, 89, 81, 973));
			param.setSourceSubsampling(2, 3, 1, 2);

			return getReader(in).read(0, param);
		});
	}

	@Test
	void testCustomDestinationImage() throws Exception {
		test(in -> {
			BufferedImage dest = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

			ImageReadParam param = new ImageReadParam();
			param.setSourceRegion(new Rectangle(99, 89, 81, 73));
			param.setSourceSubsampling(2, 3, 1, 2);
			param.setDestination(dest);
			// Out of bounds on purpose
			param.setDestinationOffset(new Point(-12, 43));

			return getReader(in).read(0, param);
		});
	}

	@Test
	void testCustomBands() throws Exception {
		test(in -> {
			BufferedImage dest = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

			ImageReadParam param = new ImageReadParam();
			param.setSourceRegion(new Rectangle(99, 89, 81, 73));
			param.setSourceSubsampling(2, 3, 1, 2);
			param.setSourceBands(new int[] {3, 2, 0});
			param.setDestination(dest);
			param.setDestinationOffset(new Point(12, 43));
			param.setDestinationBands(new int[] {0, 1, 2});

			return getReader(in).read(0, param);
		});
	}

	@Test
	void testDestinationImageTypeGray() throws Exception {
		test(in -> {
			BufferedImage dest = new BufferedImage(800, 600, BufferedImage.TYPE_BYTE_GRAY);

			ImageReadParam param = new ImageReadParam();
			param.setSourceBands(new int[] {0});
			param.setDestination(dest);

			return getReader(in).read(0, param);
		});
	}

	@Test
	void testDestinationImageTypeUShort() {
		// When reading into USHORT_555_RGB, reader outputs nonsense which does
		// not match PNG reader output; so this is forbidden for now -- reader
		// will throw an exception when decoding into non 8-bit per band images.
		Assertions.assertThrows(IIOException.class, () -> test(in -> {
			BufferedImage dest = new BufferedImage(800, 600, BufferedImage.TYPE_USHORT_555_RGB);

			ImageReadParam param = new ImageReadParam();
			param.setSourceBands(new int[] {0, 1, 2});
			param.setDestination(dest);

			return getReader(in).read(0, param);
		}));
	}

	@FunctionalInterface
	private interface ImageSupplier {

		BufferedImage get(InputStream in) throws Exception;

	}

	// Writes an image in QOI and PNG formats, then reads both
	// images using provided supplier and compares them pixel-wise.
	private void test(@NonNull ImageSupplier supplier) throws Exception {
		InputStream in = Objects.requireNonNull(QOIImageReaderTest.class.getResourceAsStream("/dice.png"), "Test image not found");

		BufferedImage original = Objects.requireNonNull(ImageIO.read(in), "Test image is invalid");

		test(original, supplier);
	}

	private void test(@NonNull BufferedImage original, @NonNull ImageSupplier supplier) throws Exception {
		ByteArrayOutputStream png = new ByteArrayOutputStream();
		ImageIO.write(original, "PNG", png);

		ByteArrayOutputStream qoi = new ByteArrayOutputStream();
		QOIUtil.writeImage(QOIUtilAWT.createFromBufferedImage(original), qoi);

		TestUtil.assertPixelsEqual(
				supplier.get(new ByteArrayInputStream(png.toByteArray())),
				supplier.get(new ByteArrayInputStream(qoi.toByteArray())),
				null
		);
	}

	private ImageReader getReader(@NonNull InputStream input) throws IOException {
		ImageInputStream in = ImageIO.createImageInputStream(input);
		ImageReader reader = ImageIO.getImageReaders(in).next();
		reader.setInput(in);
		return reader;
	}

}
