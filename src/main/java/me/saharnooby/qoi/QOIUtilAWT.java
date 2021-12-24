package me.saharnooby.qoi;

import lombok.NonNull;
import me.saharnooby.qoi.plugin.QOIImageReader;
import me.saharnooby.qoi.plugin.QOIImageWriter;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

/**
 * Contains public API methods of the library.
 */
public final class QOIUtilAWT {

	/**
	 * Converts a {@link RenderedImage} into a QOI image. Image data is copied.
	 * @param image Source image.
	 * @return Conversion result.
	 */
	public static QOIImage createFromRenderedImage(@NonNull RenderedImage image) {
		return QOIImageWriter.createFromRenderedImage(image);
	}

	/**
	 * Converts a {@link BufferedImage} into a QOI image. Image data is copied.
	 * @param image Source image.
	 * @return Conversion result.
	 */
	public static QOIImage createFromBufferedImage(@NonNull BufferedImage image) {
		return QOIImageWriter.createFromRenderedImage(image);
	}

	/**
	 * Converts a QOI image into a {@link BufferedImage}. Image data is not copied.
	 * @param image Source image.
	 * @return Conversion result.
	 */
	public static BufferedImage convertToBufferedImage(@NonNull QOIImage image) {
		return QOIImageReader.convertToBufferedImage(image);
	}

}
