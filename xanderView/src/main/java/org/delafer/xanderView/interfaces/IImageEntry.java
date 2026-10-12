package org.delafer.xanderView.interfaces;

import java.util.HashMap;
import java.util.Map;

import org.delafer.xanderView.common.ImageSize;
import org.delafer.xanderView.file.entry.ImageAbstract;
import org.delafer.xanderView.file.entry.ImageAbstract.ImageType;

@SuppressWarnings("serial")
public interface IImageEntry<E> extends IFileEntry<E> {

	static final int X = -1;

	final static Map<String, ImageType> types= new HashMap<String, ImageAbstract.ImageType>(16, 1f) {{
		put("jpg", 	ImageType.JPEG);
		put("jpeg", ImageType.JPEG);
		put("jpe", 	ImageType.JPEG);
		put("jfif", ImageType.JPEG);
		put("jif", 	ImageType.JPEG);
		put("jfi", 	ImageType.JPEG);
		put("bmp", 	ImageType.BMP);
		put("rle", 	ImageType.BMP);
		put("dib", 	ImageType.BMP);
		put("png", 	ImageType.PNG);
		put("avif",	ImageType.AVIF);
		put("webp",	ImageType.WEBP);
		put("jxl",	ImageType.JXL);
		put("aim",	ImageType.AIM);
		put("cry", 	ImageType.ENCRYPTED);
		put("xav", 	ImageType.XAV);
	}};

	public ImageSize getImageSize();


}
