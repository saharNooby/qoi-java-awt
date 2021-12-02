package me.saharnooby.qoi;

import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author saharNooby
 * @since 16:12 02.12.2021
 */
class QOIUtilAWTTest {

	private static final int[] BUFFERED_IMAGE_TYPES = new int[] {
			BufferedImage.TYPE_INT_RGB,
			BufferedImage.TYPE_INT_ARGB,
			BufferedImage.TYPE_INT_ARGB_PRE,
			BufferedImage.TYPE_INT_BGR,
			BufferedImage.TYPE_3BYTE_BGR,
			BufferedImage.TYPE_4BYTE_ABGR,
			BufferedImage.TYPE_4BYTE_ABGR_PRE
	};

	@Test
	void testOrange() throws Exception {
		test("orange.png");
	}

	@Test
	void testOrangeCross() throws Exception {
		test("orange-cross.png");
	}

	private void test(@NonNull String imageName) throws Exception {
		InputStream in = Objects.requireNonNull(getClass().getResourceAsStream("/" + imageName), imageName);

		BufferedImage source = Objects.requireNonNull(ImageIO.read(in), imageName);

		int width = source.getWidth();
		int height = source.getHeight();

		for (int type : BUFFERED_IMAGE_TYPES) {
			BufferedImage copy = new BufferedImage(width, height, type);

			{
				Graphics g = copy.getGraphics();
				g.drawImage(source, 0, 0, null);
				g.dispose();
			}

			QOIImage qoi = QOIUtilAWT.createFromBufferedImage(copy);

			String message = "image type " + type + ", " + qoi.getChannels() + " channels";

			// Check that pixel data is correct
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int i = (y * width + x) * qoi.getChannels();

					int r = qoi.getPixelData()[i] & 0xFF;
					int g = qoi.getPixelData()[i + 1] & 0xFF;
					int b = qoi.getPixelData()[i + 2] & 0xFF;
					int a = qoi.getChannels() == 4 ? qoi.getPixelData()[i + 3] & 0xFF : 0xFF;

					Assertions.assertEquals(copy.getRGB(x, y), (a << 24) | (r << 16) | (g << 8) | b, message);
				}
			}

			BufferedImage convertedBack = QOIUtilAWT.convertToBufferedImage(qoi);

			// Check that image was not corrupted during encoding process
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					Assertions.assertEquals(copy.getRGB(x, y), convertedBack.getRGB(x, y), message);
				}
			}
		}
	}

}