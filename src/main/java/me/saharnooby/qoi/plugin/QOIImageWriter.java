package me.saharnooby.qoi.plugin;

import lombok.NonNull;
import me.saharnooby.qoi.QOIColorSpace;
import me.saharnooby.qoi.QOIImage;
import me.saharnooby.qoi.QOIUtil;
import me.saharnooby.qoi.QOIUtilAWT;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.*;
import java.io.IOException;

public final class QOIImageWriter extends ImageWriter {

	QOIImageWriter(@NonNull ImageWriterSpi originatingProvider) {
		super(originatingProvider);
	}

	@Override
	public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
		// Metadata is not supported
		return null;
	}

	@Override
	public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
		// Metadata is not supported
		return null;
	}

	@Override
	public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
		// Metadata is not supported
		return null;
	}

	@Override
	public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
		// Metadata is not supported
		return null;
	}

	@Override
	public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
		clearAbortRequest();

		processImageStarted(0);

		RenderedImage rendered = image.getRenderedImage();

		// Fast path
		if (param == null || isDefault(param)) {
			writeImage(QOIUtilAWT.createFromRenderedImage(rendered));

			return;
		}

		Rectangle sourceRegion = new Rectangle(0, 0, rendered.getWidth(), rendered.getHeight());

		if (param.getSourceRegion() != null) {
			sourceRegion = sourceRegion.intersection(param.getSourceRegion());
		}

		int sourceXSubsampling = param.getSourceXSubsampling();
		int sourceYSubsampling = param.getSourceYSubsampling();
		int[] sourceBands = param.getSourceBands();

		int subsamplingXOffset = param.getSubsamplingXOffset();
		int subsamplingYOffset = param.getSubsamplingYOffset();
		sourceRegion.x += subsamplingXOffset;
		sourceRegion.y += subsamplingYOffset;
		sourceRegion.width -= subsamplingXOffset;
		sourceRegion.height -= subsamplingYOffset;

		int width = sourceRegion.width;
		int height = sourceRegion.height;

		Raster raster = rendered.getData(sourceRegion);

		int bandCount = sourceBands == null ? raster.getNumBands() : sourceBands.length;

		if (bandCount != 3 && bandCount != 4) {
			throw new IllegalArgumentException("Band count not supported");
		}

		if (sourceBands != null) {
			for (int sourceBand : sourceBands) {
				if (sourceBand >= raster.getNumBands()) {
					throw new IllegalArgumentException("Invalid band");
				}
			}
		}

		raster = raster.createChild(
				sourceRegion.x,
				sourceRegion.y,
				width,
				height,
				0,
				0,
				sourceBands
		);

		width = (width + sourceXSubsampling - 1) / sourceXSubsampling;
		height = (height + sourceYSubsampling - 1) / sourceYSubsampling;

		byte[] pixels = new byte[width * height * bandCount];

		int[] pixel = new int[bandCount];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// todo should we here convert the pixel using the ColorModel?
				raster.getPixel(x * sourceXSubsampling, y * sourceYSubsampling, pixel);

				int i = index(x, y, width, bandCount);

				pixels[i] = (byte) pixel[0];
				pixels[i + 1] = (byte) pixel[1];
				pixels[i + 2] = (byte) pixel[2];

				if (bandCount == 4) {
					pixels[i + 3] = (byte) pixel[3];
				}
			}

			processImageProgress(y * 100F / height);

			if (abortRequested()) {
				processWriteAborted();

				return;
			}
		}

		writeImage(QOIUtil.createFromPixelData(pixels, width, height, bandCount));
	}

	private void writeImage(@NonNull QOIImage converted) throws IOException {
		ImageOutputStream output = (ImageOutputStream) this.output;

		QOIUtil.writeImage(converted, new WrappedImageOutputStream(output));

		output.flush();

		processImageComplete();
	}

	public static QOIImage createFromRenderedImage(@NonNull RenderedImage image) {
		if (image instanceof BufferedImage) {
			return createFromBufferedImage((BufferedImage) image);
		}

		return createFromRaster(image.getData(), image.getColorModel());
	}

	private static QOIImage createFromBufferedImage(@NonNull BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		int channels = image.getTransparency() != Transparency.OPAQUE ? 4 : 3;

		byte[] pixelData = new byte[width * height * channels];

		switch (image.getType()) {
			case BufferedImage.TYPE_INT_RGB:
			case BufferedImage.TYPE_INT_ARGB: {
				int[] pixel = new int[1];

				Raster raster = image.getRaster();

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

				Raster raster = image.getRaster();

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

				Raster raster = image.getRaster();

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

				Raster raster = image.getRaster();

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
			default:
				return createFromRaster(image.getRaster(), image.getColorModel());
		}

		return QOIUtil.createFromPixelData(pixelData, width, height, channels, QOIColorSpace.SRGB);
	}

	// Slowest method
	private static QOIImage createFromRaster(@NonNull Raster raster, @NonNull ColorModel colorModel) {
		int width = raster.getWidth();
		int height = raster.getHeight();
		int channels = colorModel.getTransparency() != Transparency.OPAQUE ? 4 : 3;

		byte[] pixelData = new byte[width * height * channels];

		Object pixel = null;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pixel = raster.getDataElements(x, y, pixel);

				setRGB(pixelData, x, y, width, channels, colorModel.getRGB(pixel));
			}
		}

		return QOIUtil.createFromPixelData(pixelData, width, height, channels, QOIColorSpace.SRGB);
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

	private static boolean isDefault(@NonNull ImageWriteParam param) {
		return param.getClass() == ImageWriteParam.class &&
				param.getSourceRegion() == null &&
				param.getSourceXSubsampling() == 1 &&
				param.getSourceYSubsampling() == 1 &&
				param.getSubsamplingXOffset() == 0 &&
				param.getSubsamplingYOffset() == 0 &&
				param.getSourceBands() == null &&
				param.getDestinationType() == null &&
				param.getDestinationOffset().equals(new Point(0, 0));
	}

}
