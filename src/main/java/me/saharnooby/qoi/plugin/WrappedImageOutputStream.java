package me.saharnooby.qoi.plugin;

import lombok.NonNull;

import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Wraps an {@link ImageOutputStream} into an {@link OutputStream}.
 */
final class WrappedImageOutputStream extends OutputStream {

	private final ImageOutputStream output;

	public WrappedImageOutputStream(@NonNull ImageOutputStream output) {
		this.output = output;
	}

	@Override
	public void write(byte @NonNull [] b) throws IOException {
		this.output.write(b);
	}

	@Override
	public void write(byte @NonNull [] b, int off, int len) throws IOException {
		this.output.write(b, off, len);
	}

	@Override
	public void write(int b) throws IOException {
		this.output.write(b);
	}

}
