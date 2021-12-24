package me.saharnooby.qoi.plugin;

import lombok.NonNull;
import me.saharnooby.qoi.QOIUtil;
import me.saharnooby.qoi.QOIUtilAWT;
import me.saharnooby.qoi.util.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author saharNooby
 * @since 16:01 23.12.2021
 */
class QOIImageWriterTest {

	@Test
	void testDefaultParams() throws Exception {
		test(param -> {});
	}

	@Test
	void testCustomSourceRegionWithSubsampling() throws Exception {
		test(param -> {
			// Out of bounds on purpose
			param.setSourceRegion(new Rectangle(99, 89, 81, 973));
			param.setSourceSubsampling(2, 3, 1, 2);
		});
	}

	@Test
	void testCustomDestinationOffset() throws Exception {
		test(param -> {
			param.setSourceRegion(new Rectangle(99, 89, 81, 73));
			param.setSourceSubsampling(2, 3, 1, 2);
			// Out of bounds on purpose
			param.setDestinationOffset(new Point(-12, 43));
		});
	}

	@Test
	void testCustomSourceBands() throws Exception {
		test(param -> {
			param.setSourceRegion(new Rectangle(99, 89, 81, 73));
			param.setSourceSubsampling(2, 3, 1, 2);
			param.setSourceBands(new int[] {3, 2, 0});
			param.setDestinationOffset(new Point(12, 43));
		});
	}

	@Test
	void testUnsupportedImageTypes() {
		// For now we support only 3 or 4 channel, 8-bit per channel images.
		Assertions.assertThrows(ImageWriterNotFoundException.class, () -> test(new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_GRAY), param -> {}));
		Assertions.assertThrows(ImageWriterNotFoundException.class, () -> test(new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_INDEXED), param -> {}));
		Assertions.assertThrows(ImageWriterNotFoundException.class, () -> test(new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_BINARY), param -> {}));
		Assertions.assertThrows(ImageWriterNotFoundException.class, () -> test(new BufferedImage(16, 16, BufferedImage.TYPE_USHORT_555_RGB), param -> {}));
		Assertions.assertThrows(ImageWriterNotFoundException.class, () -> test(new BufferedImage(16, 16, BufferedImage.TYPE_USHORT_565_RGB), param -> {}));
	}

	// Writes an image in QOI and PNG formats using provided set-up
	// function, then reads both images and compares them pixel-wise.
	private void test(@NonNull Consumer<ImageWriteParam> setup) throws IOException {
		InputStream in = Objects.requireNonNull(QOIImageReaderTest.class.getResourceAsStream("/dice.png"), "Test image not found");

		BufferedImage original = Objects.requireNonNull(ImageIO.read(in), "Test image is invalid");

		test(original, setup);
	}

	private void test(@NonNull BufferedImage original, @NonNull Consumer<ImageWriteParam> setup) throws IOException {
		byte[] png = write(original, "PNG", setup);
		byte[] qoi = write(original, "QOI", setup);

		BufferedImage qoiImage = QOIUtilAWT.convertToBufferedImage(QOIUtil.readImage(new ByteArrayInputStream(qoi)));
		BufferedImage pngImage = ImageIO.read(new ByteArrayInputStream(png));

		TestUtil.assertPixelsEqual(pngImage, qoiImage, null);
	}

	private static byte[] write(@NonNull BufferedImage image, @NonNull String format, @NonNull Consumer<ImageWriteParam> setup) throws IOException {
		Iterator<ImageWriter> iterator = ImageIO.getImageWriters(ImageTypeSpecifier.createFromRenderedImage(image), format);

		if (!iterator.hasNext()) {
			throw new ImageWriterNotFoundException();
		}

		ImageWriter writer = iterator.next();

		ImageWriteParam param = writer.getDefaultWriteParam();

		setup.accept(param);

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		ImageOutputStream output = ImageIO.createImageOutputStream(out);
		writer.setOutput(output);
		writer.write(null, new IIOImage(image, null, null), param);
		writer.dispose();
		output.flush();

		return out.toByteArray();
	}

}
