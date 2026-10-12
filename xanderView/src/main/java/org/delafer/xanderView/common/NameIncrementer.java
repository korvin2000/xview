package org.delafer.xanderView.common;

import java.util.*;

import net.j7.commons.types.DoubleValue;

public class NameIncrementer implements INameIncrementer {


	private String value;
	private int radix;
	private List<DoubleValue<Character, Integer>> skipped;

    final static char[] digits = {
        '0' , '1' , '2' , '3' , '4' , '5' ,
        '6' , '7' , '8' , '9' , 'a' , 'b' ,
        'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
        'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
        'o' , 'p' , 'q' , 'r' , 's' , 't' ,
        'u' , 'v' , 'w' , 'x' , 'y' , 'z'
    };

    private static Map<Character, Integer> radixMap;

    static {
    	radixMap = new HashMap<Character, Integer>(digits.length);
    	for (int i = 0; i < digits.length; i++) {
			radixMap.put(Character.valueOf(digits[i]), Integer.valueOf(i+1));
		}
    }

	public NameIncrementer(String name) {

		StringBuilder sb = new StringBuilder(name.length());
		List<DoubleValue<Character, Integer>> skipped = new LinkedList<DoubleValue<Character, Integer>>();
		char ch, lch;
		int max = 10;
		for (int i = name.length()-1; i >= 0; i--) {
			ch = name.charAt(i);
			lch = Character.toLowerCase(ch);
			if (isNumeric(lch)&&(sb.length()<6)) {
				sb.insert(0, lch);
				Integer radix = radixMap.get(Character.valueOf(lch));
				if (radix.intValue() > max) max = radix.intValue();
			} else {
				skipped.add(0, new DoubleValue<Character, Integer>(ch, i));
			}
		}

		if (sb.length()==0) sb.append('0');
//		System.out.println("x:"+max);

		init(sb.toString(), max, skipped);
	}

	private void init(String name, int radix, List<DoubleValue<Character, Integer>> skipped) {
		this.value = name;
		this.radix = radix;
		this.skipped = skipped;
	}

	public String raw() {
		return String.format("%s (%s;%s)", value, radix, skipped.size());
	}


	public NameIncrementer increment() {
		long x = Long.parseLong(value, radix);
		x++;
		this.value  = Long.toString(x, radix);
		return this;
	}


	public String build() {
		StringBuilder sb = new StringBuilder(value);
		for (DoubleValue<Character, Integer> next : skipped) {
			sb.insert(next.getTwo().intValue(), next.getOne());
		}
		return sb.toString();
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

		INameIncrementer n = new NameIncrementer("01B010");
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
