package org.delafer.xanderView.common;

public final class ImageSize {

	public int width;
	public int height;

	public ImageSize() {
	}

	public ImageSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void swapXY() {
		final int hTmp = this.height;
		this.height = this.width;
		this.width = hTmp;
	}

	public final static ImageSize as(int width, int height) {
		return new ImageSize(width, height);
	}

	public int width() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}


	public int height() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public long dims() {
		return (long)width * (long)height;
	}

	@Override
	public int hashCode() {
		int result =  width;
		return 31 * result + height;
	}

	public boolean empty() {
		return this.height == 0 && this.width == 0;
	}

	public void scale(float factor) {
		width = Math.round(factor  * width);
		height = Math.round(factor  * height);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ImageSize)) return false;

		final ImageSize o = (ImageSize) obj;
		return this.width == o.width && this.height == o.height;
	}

	@Override
	public String toString() {
		return "ImageSize [width=" + width + ", height=" + height + "]";
	}


}
