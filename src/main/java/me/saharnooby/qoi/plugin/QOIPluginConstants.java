package me.saharnooby.qoi.plugin;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

final class QOIPluginConstants {

	static final String VENDOR_NAME = "saharNooby";
	static final String VERSION = "1.1.0";
	static final String[] NAMES = {"qoi", "QOI"};
	static final String[] SUFFIXES = {"qoi"};
	static final String[] MIME_TYPES = new String[0];

	static final Class<?>[] INPUT_TYPES = {ImageInputStream.class};
	static final Class<?>[] OUTPUT_TYPES = {ImageOutputStream.class};

	static final String READER_CLASS_NAME = "me.saharnooby.qoi.plugin.QOIImageReader";
	static final String WRITER_CLASS_NAME = "me.saharnooby.qoi.plugin.QOIImageWriter";

	static final String READER_SPI_CLASS_NAME = "me.saharnooby.qoi.plugin.QOIImageReaderSPI";
	static final String WRITER_SPI_CLASS_NAME = "me.saharnooby.qoi.plugin.QOIImageWriterSPI";

}
