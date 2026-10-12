package org.delafer.xanderView.scale;

import net.j7.commons.jni.LibraryLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ResizerNewOpenCV extends ResizerBase {


	@Override
	public int getMaxFilters() {
		return 5;
	}

	static {
		LibraryLoader.loadLibrary("opencv_java460");
	}

	private static final ConcurrentMap<Integer, ResizerNewOpenCV> multitons = new ConcurrentHashMap<>();


	public static ResizerNewOpenCV instance(final int key) {
		return multitons.computeIfAbsent(key, ResizerNewOpenCV::new);
	}

	private final Integer current;

	private ResizerNewOpenCV(int current) {
		super();
		this.current = Integer.valueOf(current);
	}

	private static int types[] = new int[] {Imgproc.INTER_NEAREST, Imgproc.INTER_AREA, Imgproc.INTER_LINEAR, Imgproc.INTER_CUBIC, Imgproc.INTER_LANCZOS4 };
	private static String names[] = new String[] {"CV_NEAREST", "CV_AREA", "CV_LINEAR", "CV_CUBIC", "CV_LANCZOS4" };


	public BufferedImage resize(BufferedImage inputImage, int targetWidth, int targetHeight) {
		// Ensure the image is of type TYPE_3BYTE_BGR or TYPE_4BYTE_ABGR
		BufferedImage convertedImage = convertToSupportedFormat(inputImage);

		// Convert BufferedImage to Mat
		Mat matImage = bufferedImageToMat(convertedImage);

		// Resize using OpenCV
		Mat resizedMat = new Mat();
		try {
			Size size = new Size(targetWidth, targetHeight);
			Imgproc.resize(matImage, resizedMat, size, 0, 0, types[current()]);

			// Convert Mat back to BufferedImage
			return matToBufferedImage(resizedMat, convertedImage.getType());
		} finally {
			resizedMat.release();
			matImage.release();
		}
	}

	private static BufferedImage convertToSupportedFormat(BufferedImage image) {
		int type = image.getType();
		if (type == BufferedImage.TYPE_3BYTE_BGR || type == BufferedImage.TYPE_4BYTE_ABGR) {
			return image;
		}
		int newType = image.getColorModel().hasAlpha() ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR;
		BufferedImage convertedImg = new BufferedImage(image.getWidth(), image.getHeight(), newType);
		Graphics2D g2d = convertedImg.createGraphics();
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();
		return convertedImg;
	}

	private static Mat bufferedImageToMat(BufferedImage bi) {
		byte[] pixels = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		int type = bi.getType();
		Mat mat;

		if (type == BufferedImage.TYPE_3BYTE_BGR) {
			mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
			mat.put(0, 0, pixels);
		} else if (type == BufferedImage.TYPE_4BYTE_ABGR) {
			// Convert from ABGR to BGRA
			byte[] bgraPixels = new byte[pixels.length];
			for (int i = 0; i < pixels.length; i += 4) {
				bgraPixels[i]     = pixels[i + 1]; // B
				bgraPixels[i + 1] = pixels[i + 2]; // G
				bgraPixels[i + 2] = pixels[i + 3]; // R
				bgraPixels[i + 3] = pixels[i];     // A
			}
			mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC4);
			mat.put(0, 0, bgraPixels);
		} else {
			throw new IllegalArgumentException("Unsupported BufferedImage type: " + type);
		}
		return mat;
	}

	private static BufferedImage matToBufferedImage(Mat mat, int bufferedImageType) {
		byte[] data = new byte[(int) (mat.total() * mat.channels())];
		mat.get(0, 0, data);
		BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), bufferedImageType);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

		if (bufferedImageType == BufferedImage.TYPE_3BYTE_BGR) {
			System.arraycopy(data, 0, targetPixels, 0, data.length);
		} else if (bufferedImageType == BufferedImage.TYPE_4BYTE_ABGR) {
			// Convert from BGRA to ABGR
			for (int i = 0; i < data.length; i += 4) {
				targetPixels[i]     = data[i + 3]; // A
				targetPixels[i + 1] = data[i];     // B
				targetPixels[i + 2] = data[i + 1]; // G
				targetPixels[i + 3] = data[i + 2]; // R
			}
		} else {
			throw new IllegalArgumentException("Unsupported BufferedImage type: " + bufferedImageType);
		}

		return image;
	}

	@Override
	public String name() {
		return names[current()];
	}

	@Override
	public ResizerBase as(int filterI) {
		return ResizerNewOpenCV.instance(filterI);
	}

	@Override
	public int current() {
		return current.intValue();
	}

	public static void main(String[] args) {
		int max = ResizerNewOpenCV.instance(2).getMaxFilters();
		for (int i = 0; i < max; i++) {
			ResizerBase rs = new ResizerNewOpenCV(i);
			System.out.println("["+i+"]="+rs.name());
		}

	}

	@Override
	public String toString() {
		return String.format("OpenCV [%s]", this.name());
	}

}
