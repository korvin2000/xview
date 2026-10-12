package org.delafer.xanderView.scale;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.j7.commons.jni.LibraryLoader;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;




public class ResizerOpenCV extends ResizerBase {


	@Override
	public int getMaxFilters() {
		return 5;
	}

	static {
//		LibraryLoader.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		LibraryLoader.loadLibrary("opencv_java460");
//		LibraryLoader.loadLibrary("opencv_java452");
//		LibraryLoader.loadLibrary("opencv_java347");
//		LibraryLoader.loadLibrary("opencv_java320");
//		LibraryLoader.loadLibrary("opencv_java310");
//		LibraryLoader.loadLibrary("opencv_java300");
	}

	private static final ConcurrentMap<Integer, ResizerOpenCV> multitons = new ConcurrentHashMap<>();


	public static ResizerOpenCV instance(final int key) {
		return multitons.computeIfAbsent(key, ResizerOpenCV::new);
	}

	private final Integer current;

	private ResizerOpenCV(int current) {
		super();
		this.current = Integer.valueOf(current);
		//BufferedImage img = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
		//img = this.resize(img, 6, 6);
	}

	private static int types[] = new int[] {Imgproc.INTER_NEAREST, Imgproc.INTER_AREA, Imgproc.INTER_LINEAR, Imgproc.INTER_CUBIC, Imgproc.INTER_LANCZOS4 };
	private static String names[] = new String[] {"CV_NEAREST", "CV_AREA", "CV_LINEAR", "CV_CUBIC", "CV_LANCZOS4" };

	// Convert image to Mat
	public Mat matify(BufferedImage bufferedImage) {
	    // Convert INT to BYTE
	    //im = new BufferedImage(im.getWidth(), im.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
	    // Convert bufferedimage to byte array
		Mat image = null;
		DataBuffer db= bufferedImage.getRaster().getDataBuffer();
//		System.out.println(db.getDataType());
		if (db instanceof DataBufferByte) {
			byte[] pixels = ((DataBufferByte) db).getData();

		    // Create a Matrix the same size of image
			    image = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CvType.CV_8UC3);
		    // Fill Matrix with image values
		    image.put(0, 0, pixels);
		} else
		if (db instanceof DataBufferInt) {
			int[] pixels = ((DataBufferInt)db).getData();
			image = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CvType.CV_32SC3);
		    // Fill Matrix with image values
		    image.put(0, 0, pixels);
		}

	    return image;

	}

	public BufferedImage MatToBufferedImage(Mat resizeMat){
//	      long startTime = System.nanoTime();
	      int width = resizeMat.width(), height = resizeMat.height(), channels = resizeMat.channels() ;
	      byte[] sourcePixels = new byte[width * height * channels];
	      resizeMat.get(0, 0, sourcePixels);
	      // create new image and get reference to backing data
	      BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
	      final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	      System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);
//	      long endTime = System.nanoTime();
//	      System.out.println(String.format("Elapsed time: %.2f ms", (float)(endTime - startTime)/1000000));
	      return image;
	}
	private Mat resizeMat;

	@Override
	public BufferedImage resize(BufferedImage input, int width, int height) {
		if (null == resizeMat) {
			resizeMat = new Mat();
		}
		Size size = new Size(width, height);
		Mat mt = null;
		try {
			mt = matify(input);
		} catch (java.lang.UnsupportedOperationException u) {
			u.printStackTrace();
		}


		if (mt == null) {
			//workaround
			System.out.println("workaround "+mt);
			return new ResizerNobel(4).resize(input, width, height);
			//return new ResizerJava2D(2).resize(input, width, height);
		}
		//System.out.println("mt: "+mt+" resize: "+(resizeMat != null ? resizeMat.size() : null)+ " size: "+size+ " current: "+current);
		Imgproc.resize(mt, resizeMat, size, 0f, 0f, types[current()]);
        BufferedImage resized = MatToBufferedImage(resizeMat);
		mt.release();
        return resized;
	}

	@Override
	public String name() {
		return names[current()];
	}

	@Override
	public ResizerBase as(int filterI) {
		return ResizerOpenCV.instance(filterI);
	}

	@Override
	public int current() {
		return current.intValue();
	}

	public static void main(String[] args) {
		int max = ResizerOpenCV.instance(2).getMaxFilters();
		for (int i = 0; i < max; i++) {
			ResizerBase rs = new ResizerOpenCV(i);
			System.out.println("["+i+"]="+rs.name());
		}

	}

	@Override
	public String toString() {
		return String.format("OpenCV [%s]", this.name());
	}

}
