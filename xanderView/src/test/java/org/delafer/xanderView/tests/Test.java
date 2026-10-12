package org.delafer.xanderView.tests;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.delafer.xanderView.file.entry.Encryptor;
import org.delafer.xanderView.file.entry.Encryptor.DecData;

import net.j7.commons.io.ByteBufferFileUtils;
import net.j7.commons.io.FileUtils;

public class Test {
	
	final static boolean encrypt = true	;

	public static void main(String[] args) {
		
		for (int i = 1; i <= 11; i++) {
			String name = "a"+i+".jpg";
			
			if (encrypt)
				try {
					String input = "A:\\selection\\finest\\"+name;
					ByteBuffer b = ByteBufferFileUtils.readFromFile(input);
					ByteBuffer enc = Encryptor.encrypt(new DecData(b, input, b.remaining()));
					ByteBufferFileUtils.writeToFile("A:\\"+name+".cry", enc);
					System.out.println("done enc");
				} catch (IOException e) {
					e.printStackTrace();
				} else
				try {
					String input = "A:\\"+name+".cry";
					ByteBuffer b = ByteBufferFileUtils.readFromFile(input);
					DecData dec = Encryptor.decrypt(b);
					ByteBufferFileUtils.writeToFile("A:\\"+FileUtils.getFileName(dec.name()), dec.get());
					System.out.println("done dec");
				} catch (IOException e) {
					e.printStackTrace();
				}	
			
		}
		


	}

}
