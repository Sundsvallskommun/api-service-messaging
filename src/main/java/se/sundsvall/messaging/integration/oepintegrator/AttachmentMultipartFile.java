package se.sundsvall.messaging.integration.oepintegrator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

import static java.util.Objects.requireNonNull;

class AttachmentMultipartFile implements MultipartFile {

	private final String fileName;
	private final String mimeType;
	private final InputStream contentStream;

	AttachmentMultipartFile(final String fileName, final String mimeType, final InputStream contentStream) {
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.contentStream = contentStream;
	}

	public static AttachmentMultipartFile create(final String fileName, final String mimeType, final InputStream contentStream) {
		requireNonNull(fileName, "Filename must be provided");
		requireNonNull(mimeType, "MimeType must be provided");
		requireNonNull(contentStream, "Content stream must be provided");
		return new AttachmentMultipartFile(fileName, mimeType, contentStream);
	}

	@Override
	public String getName() {
		return fileName;
	}

	@Override
	public String getOriginalFilename() {
		return fileName;
	}

	@Override
	public String getContentType() {
		return mimeType;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public long getSize() {
		try {
			return contentStream.available();
		} catch (final IOException e) {
			return 0;
		}
	}

	@Override
	public byte[] getBytes() throws IOException {
		return contentStream.readAllBytes();
	}

	@Override
	public InputStream getInputStream() {
		return contentStream;
	}

	@Override
	public void transferTo(final File dest) throws IOException, IllegalStateException {
		try (final FileOutputStream fileOutputStream = new FileOutputStream(dest)) {
			contentStream.transferTo(fileOutputStream);
		}
	}
}
