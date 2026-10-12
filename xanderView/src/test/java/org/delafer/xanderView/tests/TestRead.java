package org.delafer.xanderView.tests;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TestRead {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public byte[] readBytes1(String a) {
		try {
			byte[] buffer = new byte[4];
			InputStream is = new FileInputStream(a);
			if (is.read(buffer) != buffer.length) { 
			    // do something 
			}
			is.close();
			return buffer;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] readBytes2(String a) {
		try {
			FileInputStream fis = new FileInputStream(a);
			FileChannel fc = fis.getChannel();
			ByteBuffer bb = ByteBuffer.allocate(4);
//			bb.order(Byte)
			fc.read(bb);
//			bb.flip();
			int n = bb.getInt();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[4];
	}
	
	public final static  byte[] readBytes3(String fileName)  {

		try {
			final RandomAccessFile fis = new RandomAccessFile(fileName, "r");
			final FileChannel fc = fis.getChannel();
			MappedByteBuffer buf = fc.map(MapMode.READ_ONLY, 0, 4);
			final byte[] bytes = new byte[4];
			buf.get(bytes);
			fc.force(true);
			fis.close();
			return bytes;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}
	
	static OpenOption[] optins = new OpenOption[] {StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS};
	
	private  byte[] readBytes4(String fileName) {
		
		try {
			
			SeekableByteChannel s = Files.newByteChannel(Paths.get(fileName), optins);
			ByteBuffer bb = ByteBuffer.allocateDirect(4);
			s.read(bb);
			s.close();
			return bb.array();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
		
	}


}
