package net.j7.commons.streams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ReusableStream  {

	private static final int EOF = -1;
	private static final int BUF_SIZE = 1024;
	private byte[] data;
	private InputStream is;
	private boolean read=false;
	private boolean lazy;
	public ReusableStream(InputStream is, boolean lazy)  {
		this.is = is;
		if (!lazy) readInputStream();
	}

	private synchronized boolean isRead() {
		return read;
	}

	private synchronized void readInputStream() {

		if (read)
			return;

		if (is != null)
		{
		try {
				if (is.markSupported())
					{
					is.mark(0);
					is.reset();
					}

				byte[] buf = new byte[BUF_SIZE];
				int read;
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				while ((read = is.read(buf))!=EOF) {
					out.write(buf, 0, read);
				}

				data = out.toByteArray();
				//ZipExtractor.writeFile("d:\\filename"+(++xxx)+".dat", data);
				//is.close();

		} catch (Exception e) {e.printStackTrace();}

		} else {
			data = new byte[0];
		}

		read = true;
	}

	public static transient int xxx = 0;

	public InputStream getInputStream()  {
		if (lazy && !isRead()) readInputStream();
		return  new ByteArrayInputStream(data);
	}






}
