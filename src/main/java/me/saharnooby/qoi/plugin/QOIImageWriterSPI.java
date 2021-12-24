package me.saharnooby.qoi.plugin;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import java.util.Locale;

import static me.saharnooby.qoi.plugin.QOIPluginConstants.*;

public final class QOIImageWriterSPI extends ImageWriterSpi {

	public QOIImageWriterSPI() {
		super(
				VENDOR_NAME,
				VERSION,
				NAMES,
				SUFFIXES,
				MIME_TYPES,
				WRITER_CLASS_NAME,
				OUTPUT_TYPES,
				new String[] {READER_SPI_CLASS_NAME},
				// Standard stream metadata is not supported
				false,
				null,
				null,
				null,
				null,
				// Standard image metadata is not supported
				false,
				null,
				null,
				null,
				null
		);
	}

	@Override
	public boolean canEncodeImage(ImageTypeSpecifier type) {
		int bands = type.getNumBands();

		for (int i = 0; i < bands; i++) {
			if (type.getBitsPerBand(i) != 8) {
				return false;
			}
		}

		return bands == 3 || bands == 4;
	}

	@Override
	public ImageWriter createWriterInstance(Object extension) {
		return new QOIImageWriter(this);
	}

	@Override
	public String getDescription(Locale locale) {
		return "QOI image writer";
	}

}
