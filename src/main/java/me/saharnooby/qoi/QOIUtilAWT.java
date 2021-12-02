package me.saharnooby.qoi;

import lombok.NonNull;

import java.awt.*;
import java.awt.image.*;
import java.util.Hashtable;

/**
 * Contains public API methods of the library.
 */
public final class QOIUtilAWT {

	private static final int[] OFFSETS_3 = {0, 1, 2};
	private static final int[] OFFSETS_4 = {0, 1, 2, 3};

	/**
	 * Converts a BufferedImage into a QOI image. Image data is copied.
	 * @param image Source image.
	 * @return Conversion result.
	 */
	public static QOIImage createFromBufferedImage(@NonNull BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();

		int channels = image.getTransparency() != Transparency.OPAQUE ? 4 : 3;

		byte[] pixelData = new byte[width * height * channels];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgb = image.getRGB(x, y);

				int i = (y * width + x) * channels;

				pixelData[i] = (byte) (rgb >> 16);
				pixelData[i + 1] = (byte) (rgb >> 8);
				pixelData[i + 2] = (byte) rgb;

				if (channels == 4) {
					pixelData[i + 3] = (byte) (rgb >> 24);
				}
			}
		}

		return new QOIImage(width, height, channels, QOIColorSpace.SRGB, pixelData);
	}

	/**
	 * Converts a QOI image into a BufferedImage. Image data is not copied.
	 * @param image Source image.
	 * @return Conversion result.
	 */
	public static BufferedImage convertToBufferedImage(@NonNull QOIImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		int channels = image.getChannels();

		boolean hasAlpha = channels == 4;

		DataBufferByte buffer = new DataBufferByte(image.getPixelData(), width * height * channels);

		WritableRaster raster = Raster.createInterleavedRaster(
				buffer,
				width,
				height,
				channels * width,
				channels,
				hasAlpha ? OFFSETS_4 : OFFSETS_3,
				new Point(0, 0)
		);

		java.awt.color.ColorSpace awtColorSpace;

		switch (image.getColorSpace()) {
			case SRGB:
			case SRGB_LINEAR_ALPHA:
				awtColorSpace = java.awt.color.ColorSpace.getInstance(java.awt.color.ColorSpace.CS_sRGB);
				break;
			case LINEAR:
				awtColorSpace = java.awt.color.ColorSpace.getInstance(java.awt.color.ColorSpace.CS_LINEAR_RGB);
				break;
			default:
				throw new RuntimeException();
		}

		ColorModel colorModel = new ComponentColorModel(
				awtColorSpace,
				hasAlpha,
				false,
				hasAlpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE,
				DataBuffer.TYPE_BYTE
		);

		return new BufferedImage(
				colorModel,
				raster,
				false,
				new Hashtable<>()
		);
	}

}
