package me.saharnooby.qoi.util;

import lombok.NonNull;
import org.junit.jupiter.api.Assertions;

import java.awt.image.BufferedImage;

/**
 * @author saharNooby
 * @since 16:03 23.12.2021
 */
public final class TestUtil {

	public static void assertPixelsEqual(@NonNull BufferedImage expected, @NonNull BufferedImage actual, String message) {
		int width = expected.getWidth();
		int height = expected.getHeight();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int eColor = expected.getRGB(x, y);
				int aColor = actual.getRGB(x, y);

				if (eColor != aColor) {
					String fullMessage = "x " + x + ", " +
							"y " + y + ", " +
							"expected " + Integer.toHexString(eColor).toUpperCase() + ", " +
							"actual " + Integer.toHexString(aColor).toUpperCase();

					if (message != null) {
						fullMessage += "\n" + message;
					}

					Assertions.fail(fullMessage);
				}
			}
		}
	}

}
