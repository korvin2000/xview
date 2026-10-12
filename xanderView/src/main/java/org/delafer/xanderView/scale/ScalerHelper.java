package org.delafer.xanderView.scale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.delafer.xanderView.common.ImageSize;

public class ScalerHelper {

	List<Element> elements;



	public ScalerHelper(String str) {
		this.elements = parseStr(str);
	}

	public int getTypeBySize(ImageSize size) {
		long lSize = size.dims();
		for (Element element : elements) {
			if (lSize <= element.imgSize) return element.type;
		}
		return elements.get(elements.size()-1).type;
	}

//	public static void main(String[] args) {
//		String a = "1;2,10000;3,500x500;4,1000x1000";
//
//		ScalerHelper sc = new ScalerHelper(a);
//		ImageSize is = new ImageSize(500, 500);
//		System.out.println(sc.getTypeBySize(is));
//	}

	private static List<Element> parseStr(String str) {
		String[] arr = str.split(";");
		List<Element> ret = new ArrayList<Element>(arr.length);

		for (String next : arr) {
			String[] tokens = next.split(",");
			if (tokens.length==1) {
				ret.add(new Element(asInt(tokens[0])));
			} else {
				ret.add(Element.valueOf(asInt(tokens[0]), tokens[1]));
			}
		}

		Collections.sort(ret);
		return ret;
	}

	protected static long asLong(String str) {
		if (str != null && str.length() > 0)
			try {
				return Long.parseLong(str.trim());
			} catch (Exception e) {
			}
		return 0;
	}


	protected static int asInt(String str) {
		if (str != null && str.length() > 0)
			try {
				return Integer.parseInt(str.trim());
			} catch (Exception e) {
			}
		return 0;
	}

	protected static class Element implements Comparable<Element> {

		long imgSize;
		int type;

		public Element(int type) {
			this.imgSize = (-1L + Long.MAX_VALUE);
			this.type = type;
		}


		public static Element valueOf(int type, String dims) {
			int idx = max(dims.indexOf('x'), dims.indexOf('*'));
			if (idx >= 0) {
				return new Element(type, ScalerHelper.asLong(dims.substring(0, idx)), ScalerHelper.asLong(dims.substring(idx+1)));
			} else {
				return new Element(type, ScalerHelper.asLong(dims));
			}
		}

		public Element(int type, long imgSize) {
			this.imgSize = imgSize;
			this.type = type;
		}

		public Element(int type, long width, long height) {
			this.imgSize = width * height;
			this.type = type;
		}

		@Override
		public int compareTo(Element o) {
			long l = (imgSize - o.imgSize);
			int res = (l > (long) Integer.MAX_VALUE) ? Integer.MAX_VALUE
					: (l < (long) Integer.MIN_VALUE) ? Integer.MIN_VALUE : (int) l;
			return res;
		}


		@Override
		public String toString() {
			return String.format("[%s,%s]", type, imgSize);
		}

	}

	public static int max(int a, int b) {
		return a>b ? a : b;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Element element : elements) {
			if (sb.length()>0) sb.append(';');
			sb.append(element.toString());
		}
		return sb.toString();
	}

}
