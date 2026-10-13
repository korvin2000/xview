package org.delafer.xanderView.gui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import dev.matrixlab.webp4j.WebPCodec;

import net.j7.commons.streams.Streams;
import net.j7.commons.strings.Args;

import org.delafer.xanderView.codec.TurboJpeg;
import org.delafer.xanderView.common.ImageSize;
import org.delafer.xanderView.file.CommonContainer;
import org.delafer.xanderView.file.CopyService;
import org.delafer.xanderView.file.entry.Buf;
import org.delafer.xanderView.file.entry.ImageAbstract;
import org.delafer.xanderView.file.entry.ImageAimDec;
import org.delafer.xanderView.file.entry.ImageDec;
import org.delafer.xanderView.gui.config.OrientationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ImageLoader {

	private static final Logger log = LoggerFactory.getLogger(ImageLoader.class);

	public abstract Dimension getSize();
	public OrientationStore orientator = OrientationStore.instance();

	protected void loadImage(CommonContainer container, ImageAbstract<?> entry, ImageCanvas panel) {
		try {
			if (entry == null) return ;
			int[] position = container.position(); // index and size from one consistent state (watcher may add/remove)
			String info = Args.fill("%1 [%2/%3]", entry.shortName(),String.valueOf(position[0]),String.valueOf(position[1]));

			loadImage(entry, info, panel);
		} catch (Exception | LinkageError e) {
			// LinkageError: a decoder native (JXL, WebP, AVIF, OpenCV) is missing or unloadable on this platform; the image is skipped instead of killing the UI loop
			log.error("Can't load image", e);
		}
	}

	static int imgcnt = 0;
	protected void loadImage(ImageAbstract<?> entry, String text, ImageCanvas panel) {
		try {
			BufferedImage img = null;
			switch (entry.getImageType()) {
			case AIM:
				loadImage(new ImageAimDec(entry), text, panel);
				break;
			/*CRY - to work correctly, it should be instantiated earlier
			case ENCRYPTED:
				loadImage(new ImageDec<>(entry), text, panel); break;
			*/
			case JPEG:
				img = loadJpegImage(entry.content().getArray(true));
				break;
			case AVIF:
				imgcnt++;
				img = loadCommonImage(entry.content().getArray(true));
				break;
			case WEBP:
				img = WebPCodec.decodeImage(entry.content().getArray(true)); // webp4j JNI (native 'webp4j'); no ImageIO reader exists
				break;
			case BMP:
			case PNG:
			case JXL:
			default:
				img = loadCommonImage(entry.content().getArray(true));
				break;
			}
			if (img == null) return ;
//			GammaFilter filter = new GammaFilter(0.4f);
//			img = filter.filter(img, null);

			if (CopyService.exists(entry)) text += "*";

			if (panel.setImage(img, text, orientator.getImageData(entry.CRC()))) {
				panel.showImage();
			}


		} catch (Exception | LinkageError e) {
			// LinkageError: a decoder native (JXL, WebP, AVIF, OpenCV) is missing or unloadable on this platform; the image is skipped instead of killing the UI loop
			log.error("Can't load image", e);
		}
	}

	private BufferedImage loadCommonImageSafe(Buf content) throws IOException {
		BufferedImage ret = loadCommonImage(content.getArray(false));
		Streams.closeSilently(content);
		return ret;
	}

	private BufferedImage loadCommonImage(byte[] content) throws IOException {
		try (InputStream input = new ByteArrayInputStream(content)) {
			long t1 = System.currentTimeMillis();
			BufferedImage ret =  ImageIO.read(input);
			long t2 = System.currentTimeMillis();
			log.debug("Decoding time: {}", t2-t1);
			return ret;
		}
	}

	public abstract ImageSize displaySize();


	protected int getDenom(ImageSize si) {
		ImageSize sm = displaySize();
		float k1 = min(si.width, si.height, true) / min(sm.width, sm.height, true);
		float k2 = min(si.width, si.height, false) / min(sm.width, sm.height, false);
		float k = k1 < k2 ? k1 : k2;
		if (k <= 1f) return 1;

		if (k >= 7.9f) return 8;
		if (k >= 3.95f) return 4;
		if (k >= 2f) return 2;
		return 1;
	}
	private float min(int a, int b, boolean min) {
		return (float) ( a < b ?  (min ? a : b) : (min ? b : a) );
	}


	protected BufferedImage loadJpegImage(byte[] bytes) throws Exception {
		try {
			// TurboJPEG over FFM: DCT-scaled decode straight into the image's byte[]; the fast DCT only pays off for large files
			return TurboJpeg.decode(bytes, (w, h) -> getDenom(new ImageSize(w, h)), bytes.length >= 1750000);
		} catch (Exception | LinkageError e) {
			// LinkageError: turbojpeg native missing or incompatible (UnsatisfiedLinkError, NoClassDefFoundError); ImageIO still decodes JPEG (PLAN B18)
			return loadCommonImage(bytes);
		}
	}

}
