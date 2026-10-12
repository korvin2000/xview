package org.delafer.xanderView.sound;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.sound.sampled.*;

import net.j7.commons.streams.Streams;

public class SoundBeep {

//	public  void beep2()
//	{ InputStream is = SoundBeep.class.getResourceAsStream("sound01.wav");
//	    try
//	    {
//	        Clip clip = AudioSystem.getClip();
//	        clip.open(AudioSystem.getAudioInputStream(is));
//	        clip.start();
//	    }
//	    catch (Exception exc)
//	    {
//	        exc.printStackTrace(System.out);
//	    }
//	}
	public final static void beep() {
		InputStream is = null;
//		AudioInputStream stream= null;
		try {
			is = new BufferedInputStream(SoundBeep.class.getResourceAsStream("/audio/sound01.wav"));
		    AudioFormat format;
		    DataLine.Info info;

		    final AudioInputStream stream = AudioSystem.getAudioInputStream(is);
		    format = stream.getFormat();
		    info = new DataLine.Info(Clip.class, format);
		    final Clip clip = (Clip) AudioSystem.getLine(info);
		    clip.open(stream);
		    clip.addLineListener(new LineListener() {
				public void update(LineEvent le) {
				    LineEvent.Type type = le.getType();
				    if (type == LineEvent.Type.CLOSE) {
				    	Streams.close(stream);
				    } else if (type == LineEvent.Type.STOP) {
				      clip.close();
				    }

				}

		    });
		    clip.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

//	public static void main(String[] args) {
//		SoundBeep b = new SoundBeep();
//		b.beep();
//		try {
//			Thread.currentThread().sleep(1500);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
