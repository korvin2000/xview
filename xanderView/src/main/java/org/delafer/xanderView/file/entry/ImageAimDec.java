package org.delafer.xanderView.file.entry;

import net.j7.commons.io.FilePath;
import net.j7.commons.io.FileUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

public class ImageAimDec<E> extends ImageAbstract<E> {

	private static final int HEADER_SIZE = 4;
	private static final int HEADER_PREFIX = 0b1001;
	private static final Map<Integer, String> FILE_TYPES = Map.of(
			0, "avif",
			1, "jxl",
			2, "jpg",
			3, "webp",
			4, "webp2",
			5, "heic",
			6, "png",
			7, "grk"
	);

	private ImageAbstract<E> si;
	private Buf buf;
	private FirstImage fi;

	public ImageAimDec(ImageAbstract<E> si) {
		this.si = si;
		this.fi = ImageAimDec.extractFirstFile(si.content().get());
		this.name = FileUtils.getBaseName(FileUtils.getFileName(this.si.name))+'.'+this.fi.extension;
		this.size = this.fi.size();
		this.crc = this.fi.crc();
		this.identifier = si.identifier;
		this.parent = si.parent;
	}

	@Override
	public long lastModified() throws IOException {
		return si.lastModified();
	}

	@Override
	protected Buf rawData(E identifier, int size) throws IOException {
		return fi.fileContent;
	}

	@Override
	protected String lastEntryId(ImageAbstract<E> ia) {
		return si.lastEntryId(si);
	}


	@Override
	public ImageAbstract<E> cloneObj() {
		ImageAimDec<E> cloned = new ImageAimDec<E>(si != null ? (ImageAbstract<E>)si.cloneObj() : null);
		cloned.fi = this.fi;
		cloned.buf = this.buf;
		cloned.parent = this.parent;
		cloned.crc = this.crc;
		cloned.size = this.size;
		cloned.identifier = this.identifier;
		cloned.name = this.name;
		cloned.imageType = this.imageType;
		return cloned;
	}

	public record FirstImage(String extension, int size, long crc, Buf fileContent) {}

	private static FirstImage extractFirstFile(ByteBuffer buffer) {
		if (buffer.remaining() < HEADER_SIZE) {
			throw new IllegalArgumentException("Buffer too small to contain header");
		}

		int header = buffer.getInt();
		int prefix = header >>> 28;
		if (prefix != HEADER_PREFIX) {
			throw new IllegalArgumentException("Invalid header prefix");
		}

		int fileType = (header >>> 25) & 0b111;
		String extension = FILE_TYPES.get(fileType);
		if (extension == null) {
			throw new IllegalArgumentException("Invalid file type");
		}

		int fileSize = header & 0x1FFFFFF;
		if (buffer.remaining() < fileSize) {
			throw new IllegalArgumentException("Buffer too small to contain file content");
		}

		long crc = buffer.getLong();

		ByteBuffer fileContent = buffer.slice().limit(fileSize);
		buffer.position(buffer.position() + fileSize);

		return new FirstImage(extension, fileSize, crc, new Buf(fileContent, buffer));
	}

	public static FirstImage extractFirstFile(byte[] array) {
		return extractFirstFile(ByteBuffer.wrap(array));
	}

//	// Example usage
//	public static void main(String[] args) {
//		// Example header: 1001 010 0000000000000000000101110
//		// This represents a .jpg file of 46 bytes
//
//		Path path = Paths.get("A:\\test\\avif.aim");
//		byte[] fileBytes = null;
//		try {
//			fileBytes = Files.readAllBytes(path);
//			// Wrap the byte array into a ByteBuffer
//			ByteBuffer byteBuffer = ByteBuffer.wrap(fileBytes);
//
//			FirstImage file = extractFirstFile(byteBuffer);
//			System.out.println("File extension: " + file.extension());
//			System.out.println("File size: " + file.size());
//			System.out.println("File content capacity: " + file.fileContent().capacity());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	public String getSourcePath() {
		return this.si.getSourcePath();
	}
}
