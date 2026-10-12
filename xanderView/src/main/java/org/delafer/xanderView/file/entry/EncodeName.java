package org.delafer.xanderView.file.entry;

import me.xuender.unidecode.Unidecode;

public class EncodeName {

	public EncodeName() {
		// TODO Auto-generated constructor stub
	}
	
   public static String transliterate(String input) {
   	 return Unidecode.decode(input);
   }

	public static String correctFileName(String name) {
		   if (name == null) return null;
		    int len = name.length();
			StringBuilder sb = new StringBuilder(len);
			char ch;
			for (int i = 0; i < len; i++) {
				ch = name.charAt(i);
				if ((ch<' ') ||
					(ch==',' || ch=='/' || ch=='\\' || ch=='"' ||
					 ch=='?' || ch==';' || ch== ':' || ch=='>' ||
					 ch=='<' || ch=='|' || ch=='*')) {
						ch = '_';
					}
				sb.append(ch);
			}
			return sb.toString().trim();
	   }

	static char[] chars = new char[] {' ','~','!', '@', '#', '$', '%', '^', '&', '(', ')', '-', '+', '_','=','[',']', '{','}'};

//	public static void main3(String[] args) {
//		Map<Character, Character> a = new TreeMap<>();
//		List<Character> b = new LinkedList<>();
//
//		for (int i = 0; i < 26; i++) {
//			char c = (char)(65 + i);
//			b.add(Character.valueOf(c));
//		}
//		try {
//		SecureRandom s = SecureRandom.getInstanceStrong();
//			String a1 = "", b1 = "";
//			Map<Integer, Integer> xx = new TreeMap<Integer, Integer>();
//			for (int i = 0; i < 26; i++) {
//				Character f = Character.valueOf((char)(65 + i));
//				Character t;
//				int x;
//				do {
//					x =  s.nextInt(b.size());
//					t = b.get(x);
//				} while (f.equals(t));
//				b.remove(x);
////				System.out.println("case '"+f+"': return "+((int)f-(int)t)+";");
//				a1 += ((int)f-(int)t)+",";
//				b1 += ((int)t-(int)f)+",";
//				xx.put(((int)t-65), ((int)f - 65));
//			}
//			System.out.println(a1);
//			System.out.println();
////			System.out.println(b1);
//			for (Map.Entry<Integer, Integer> x : xx.entrySet()) {
//				System.out.print(x.getKey()-x.getValue()+",");
//			}
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		}
//	}

	final static int[] codes = new int[] {-8,-6,1,-9,-12,-10,2,5,-6,-10,-12,8,-8,2,-11,-2,16,8,5,-5,-1,16,16,5,1,15};
	final static int[] codes2 = new int[] {-16,-1,-5,-8,-2,-16,-16,6,8,-8,-15,-2,9,-5,6,10,12,2,-5,10,8,1,12,-1,5,11};
	final static int MAX_LEN = 28;
	public static String encrypt(String name) {

		if (name == null || name.length() == 0) return name;
		if (name.length()>MAX_LEN) name = name.substring(0, MAX_LEN);

		name = correctFileName(transliterate(name));
		char ch, uch;

		StringBuilder sb = new StringBuilder(name.length());

		for (int i = 0; i < name.length(); i++) {
			ch = name.charAt(i);
			if (Character.isLetter(ch)) {
				boolean uc = Character.isUpperCase(ch);
				uch = Character.toUpperCase(ch);
				int code = (int)uch - 65;
				int ncode = codes[code];
				char r = ((char)shift(((int)uch - ncode), i));
				sb.append(uc ? r : Character.toLowerCase(r));
			} else
			{
				sb.append(shf(special0(ch), i));
			}
		}
		return sb.toString();
	}

	private static int code(char ch) {
		switch (ch) {
		case ' ': return 0;
		case '~': return 1;
		case '!': return 2;
		case '@': return 3;
		case '#': return 4;
		case '$': return 5;
		case '%': return 6;
		case '^': return 7;
		case '&': return 8;
		case '(': return 9;
		case ')': return 10;
		case '-': return 11;
		case '+': return 12;
		case '_': return 13;
		case '=': return 14;
		case '[': return 15;
		case ']': return 16;
		case '{': return 17;
		case '}': return 18;
		default: return -1;
		}
	}

	private static char special0(char ch) {
		switch (ch) {
		case ' ': return '_';
		case '~': return '@';
		case '!': return '[';
		case '@': return '}';
		case '#': return '&';
		case '$': return '^';
		case '%': return '~';
		case '^': return ')';
		case '&': return '!';
		case '(': return '-';
		case ')': return '#';
		case '-': return ' ';
		case '+': return '=';
		case '_': return '$';
		case '=': return '(';
		case '[': return '%';
		case ']': return '{';
		case '{': return '+';
		case '}': return ']';
		default:
			return ch;
		}
	}

	private static char special1(char ch) {
		switch (ch) {
		case '_': return ' ';
		case '@': return '~';
		case '[': return '!';
		case '}': return '@';
		case '&': return '#';
		case '^': return '$';
		case '~': return '%';
		case ')': return '^';
		case '!': return '&';
		case '-': return '(';
		case '#': return ')';
		case ' ': return '-';
		case '=': return '+';
		case '$': return '_';
		case '(': return '=';
		case '%': return '[';
		case '{': return ']';
		case '+': return '{';
		case ']': return '}';
		default:
			return ch;
		}
	}

	public static String decrypt(String name) {
		if (name == null || name.length() == 0) return name;
		char ch, uch;

		StringBuilder sb = new StringBuilder(name.length());

		for (int i = 0; i < name.length(); i++) {
			ch = name.charAt(i);
			if (Character.isLetter(ch)) {
				boolean uc = Character.isUpperCase(ch);
				uch = Character.toUpperCase(ch);
				uch = (char)(shift2((int)uch, i));
//				System.out.println(":"+uch);
				int code = (int)uch - 65;
				int ncode = codes2[code];
				char r = ((char)((int)uch - ncode));
				sb.append(uc ? r : Character.toLowerCase(r));
			} else {
				sb.append(special1(shf2(ch, i)));
			}
		}
		return sb.toString();
	}

	private static int shift(int ch, int order ) {
		int co = ch - 65;
		co += (hashCode(order) % 26);
		return (co % 26) + 65;
	}

	private static int hashCode(int order) {
		return (order + "$").hashCode();
	}

	private static int shift2(int ch, int order ) {
		int co = (ch - 65) + 26;
		co -= (hashCode(order) % 26);
		return (co % 26) + 65 ;
	}

	private static char shf(char ch, int order ) {
		int co = code(ch);
		if (co < 0) return ch;
		co += (order % 19);
		return chars[(co % 19)];
	}

	private static char shf2(char ch, int order ) {
		int co = code(ch);
		if (co < 0) return ch;
		co += 19;
		co -= (order % 19);
		return chars[(co % 19)];
	}


//	public static void main(String[] args) {
//		String x = encrypt("In the Java programming language language, every class implicitly or explicitly provides a hashCode() method");
//		System.out.println("Encrypted: "+x);
//		System.out.println("Decrypted: "+decrypt(x));
////		System.out.println((encrypt("ASTalavista baby")));
////		System.out.println((encrypt("AAAB")));
////		System.out.println((encrypt("AABB")));
////		System.out.println(shift2(shift(69, 133), 133));
//	}
}
