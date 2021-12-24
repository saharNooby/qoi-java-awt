package me.saharnooby.qoi.plugin;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Locale;

import static me.saharnooby.qoi.plugin.QOIPluginConstants.*;

public final class QOIImageReaderSPI extends ImageReaderSpi {

	public QOIImageReaderSPI() {
		super(
				VENDOR_NAME,
				VERSION,
				NAMES,
				SUFFIXES,
				MIME_TYPES,
				READER_CLASS_NAME,
				INPUT_TYPES,
				new String[] {WRITER_SPI_CLASS_NAME},
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
	public boolean canDecodeInput(Object source) throws IOException {
		if (!(source instanceof ImageInputStream)) {
			return false;
		}

		ImageInputStream in = (ImageInputStream) source;
		byte[] b = new byte[4];
		in.mark();
		in.readFully(b);
		in.reset();

		return b[0] == (byte) 'q' &&
				b[1] == (byte) 'o' &&
				b[2] == (byte) 'i' &&
				b[3] == (byte) 'f';
	}

	@Override
	public ImageReader createReaderInstance(Object extension) {
		return new QOIImageReader(this);
	}

	@Override
	public String getDescription(Locale locale) {
		return "QOI image reader";
	}

}
