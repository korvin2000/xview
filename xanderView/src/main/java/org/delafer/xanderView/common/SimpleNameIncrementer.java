package org.delafer.xanderView.common;

import java.util.*;

import net.j7.commons.strings.StringUtils;
import net.j7.commons.types.DoubleValue;

public class SimpleNameIncrementer implements INameIncrementer {


	private String value;
	private List<DoubleValue<Character, Integer>> skipped;
	int j = 0;
	String original;

	public SimpleNameIncrementer(String name) {

		StringBuilder sb = new StringBuilder(name.length());
		List<DoubleValue<Character, Integer>> skipped = new LinkedList<DoubleValue<Character, Integer>>();
		char ch, lch;
		for (int i = name.length()-1; i >= 0; i--) {
			ch = name.charAt(i);
			lch = Character.toLowerCase(ch);
			if (isNumeric(lch)&&(sb.length()<6)) {
				sb.insert(0, lch);
			} else {
				skipped.add(0, new DoubleValue<Character, Integer>(ch, i));
			}
		}

		if (sb.length()==0) sb.append('1');

		this.init(name, sb.toString(),  skipped);
	}

	private void init(String original, String name, List<DoubleValue<Character, Integer>> skipped) {
		this.value = name;
		this.skipped = skipped;
		this.original = original;
	}

	public String raw() {
		return String.format("%s (%s;%s)", value, 10, skipped.size());
	}


	@Override
	public INameIncrementer increment() {
		long x = Long.parseLong(value);
		x++;
		j++;
		this.value  = StringUtils.fillString(Long.toString(x), value.length(), '0');
		return this;
	}


	@Override
	public String build() {
		if (j == 0) return original.trim();
		StringBuilder sb = new StringBuilder(value);
		for (DoubleValue<Character, Integer> next : skipped) {
			sb.insert(next.getTwo().intValue(), next.getOne());
		}
		return sb.toString().trim();
	}


	private static boolean isNumeric(char lch) {
		if (lch>='0'&&lch<='9') return true;
//		if (lch>='a'&&lch<='z') return true;
		return false;
	}

	public static void main(String[] args) {


//		String a = "0123456789abcdefghijklmnopqrstuvwxyz";
//
//		for (int j = 0; j < 50; j++) {
//			long r = RandomUtil.getRandomLong(0, 995999999);
//			String str = Long.toString(r, a.length());
//			Long r2 = Long.parseLong(str, a.length());
//
//			System.out.println("Orig: "+r+" str: "+str);
////			System.out.println("r2: "+r2+" r3: "+r3);
//
//			if (r2.longValue() != r) {
//				System.out.println("Error! Assertion error");
//				System.exit(-1);
//			}
//		}

		INameIncrementer n = INameIncrementer.instance("01B010");
		System.out.println(n.build());
		for (int i = 0; i < 100 ; i++) {
			n.increment();
			System.out.println(n.build());
		}




	}


	@Override
	public String toString() {
		return build();
	}

}
