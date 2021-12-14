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

		switch (image.getType()) {
			case BufferedImage.TYPE_INT_RGB:
			case BufferedImage.TYPE_INT_ARGB: {
				int[] pixel = new int[1];

				WritableRaster raster = image.getRaster();

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						raster.getDataElements(x, y, pixel);

						setRGB(pixelData, x, y, width, channels, pixel[0]);
					}
				}

				break;
			}
			case BufferedImage.TYPE_INT_BGR: {
				int[] pixel = new int[1];

				WritableRaster raster = image.getRaster();

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						raster.getDataElements(x, y, pixel);

						setRGB(pixelData, x, y, width, channels, Integer.reverseBytes(pixel[0]) >> 8);
					}
				}

				break;
			}
			case BufferedImage.TYPE_3BYTE_BGR: {
				byte[] pixel = new byte[3];

				WritableRaster raster = image.getRaster();

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						raster.getDataElements(x, y, pixel);

						int i = index(x, y, width, channels);

						pixelData[i] = pixel[0];
						pixelData[i + 1] = pixel[1];
						pixelData[i + 2] = pixel[2];
					}
				}

				break;
			}
			case BufferedImage.TYPE_4BYTE_ABGR: {
				byte[] pixel = new byte[4];

				WritableRaster raster = image.getRaster();

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						raster.getDataElements(x, y, pixel);

						int i = index(x, y, width, channels);

						pixelData[i] = pixel[0];
						pixelData[i + 1] = pixel[1];
						pixelData[i + 2] = pixel[2];
						pixelData[i + 3] = pixel[3];
					}
				}

				break;
			}
			default: {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						setRGB(pixelData, x, y, width, channels, image.getRGB(x, y));
					}
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

	private static void setRGB(byte @NonNull [] pixelData, int x, int y, int width, int channels, int rgb) {
		int i = index(x, y, width, channels);

		pixelData[i] = (byte) (rgb >> 16);
		pixelData[i + 1] = (byte) (rgb >> 8);
		pixelData[i + 2] = (byte) rgb;

		if (channels == 4) {
			pixelData[i + 3] = (byte) (rgb >> 24);
		}
	}

	private static int index(int x, int y, int width, int channels) {
		return (y * width + x) * channels;
	}

}
