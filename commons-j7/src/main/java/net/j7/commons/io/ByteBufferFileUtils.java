package net.j7.commons.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class ByteBufferFileUtils {

	
	public static void writeToFile(String name, ByteBuffer bb) {
		try {
			File file = new File(name);

			boolean append = false;
			FileOutputStream fos = new FileOutputStream(file, append);
			FileChannel wChannel = fos.getChannel();

			bb.rewind();
			
			wChannel.write(bb);
			wChannel.force(true);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public final static ByteBuffer readFromFile(String fileName) throws IOException {
		RandomAccessFile fis = null;
		MappedByteBuffer buf = null;
		try {
			fis = new RandomAccessFile(fileName, "r");
			final FileChannel fc = fis.getChannel();
			int size = (int)fc.size();
			buf = fc.map(MapMode.READ_ONLY, 0, size);
			return buf;
		} finally {
		    fis.close();
//		    closeDirectBuffer(buf);
		}
	}
	


//@SuppressWarnings("restriction")
//public static void closeDirectBuffer(ByteBuffer cb) {
//    if (!cb.isDirect()) return;
//    try {
//    	((sun.nio.ch.DirectBuffer)cb).cleaner().clean();
//    } catch(Exception ex) { }
//    cb = null;
//}
	
}
