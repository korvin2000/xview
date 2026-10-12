package net.j7.commons.strings;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The Class LongNamesSplitter.
 */
public class LongNamesSplitter {


	/** The Constant MAX_LENGTH. */
	private final static int MAX_LENGTH = 250; //max allowed name length, max total length name1+name2+name = 3 * MAX_LENGTH

	/** The Constant MX. */
	private final static int MX = 3;

	/** The Constant splitSym. */
	private static final Set<Character> splitSym;
	static {
		splitSym = new HashSet<Character>();
		splitSym.add(' ');  splitSym.add('-');
		splitSym.add('.');  splitSym.add(',');
		splitSym.add(';'); 	splitSym.add('/');
		splitSym.add('\\'); splitSym.add('+');
	}

	/**
	 * Checks names length and splits if necessary.
	 *
	 * @param names the names
	 * @return the names
	 */
	public static Names split(String... names) {
		Names ret = new Names();
		if (names == null || names.length==0) return ret;

		if (names.length<=MX && isLengthOk(names)) {
			int i = 0;
			for (String next: names) {
				ret.setName(i++, next);
			}
			return ret;
		}

		StringBuilder sb = new StringBuilder(MAX_LENGTH * MX);
		for (String next : names) {
			if (next!=null && !next.isEmpty()) sb.append(next);
		}
		String fullName = sb.toString().trim();
		if (fullName.length() > (MAX_LENGTH * MX)) {
			fullName = fullName.substring(0, MAX_LENGTH * MX);
		}

		StringBuilder sbName = new StringBuilder(fullName);
		findSplitPoint(ret, sbName, 1);


		return ret;
	}

	/**
	 * Checks if is split symbole.
	 *
	 * @return true, if is split symbole
	 */
	public static boolean isSplitSymbole(char ch){
		return splitSym.contains(ch);
	}


	/**
	 * Find split point.
	 *
	 * @param names the names
	 * @param str the string
	 * @param num the name number
	 */
	private static void findSplitPoint(Names names, StringBuilder str, int num) {
		int len = str.length();
		if (len==0 || num>MX) return ;

		int atStart = min(len, MAX_LENGTH)-1;
		int at = atStart;

		boolean searchNext;
		do {
		char ch = str.charAt(at);


		int remains = str.length() - (at+1);
		int remainsMax = ((MX-num) * MAX_LENGTH);
		boolean isSplit = isSplitSymbole(ch);
		boolean found = isSplit || (remains >= remainsMax) || (at == 0);

		searchNext = !found;

		if (found) {

			if (!isSplit) at = atStart;

			CharSequence namePart = str.subSequence(0, at+1);
			names.setName(num-1, namePart.toString());
			str.delete(0, at+1);
			findSplitPoint(names, str, num+1);
		}

		at--;
		} while (searchNext);

	}


	/**
	 * Finds Min value.
	 *
	 * @param x the x
	 * @param y the y
	 * @return the int
	 */
	private static int min(int x, int y) {
		return x<y?x:y;
	}


	/**
	 * Checks if is length is ok / passed.
	 *
	 * @param names the names
	 * @return true, if is length ok
	 */
	private static boolean isLengthOk(String[] names) {
		for (String next : names) {
			if (next != null && next.length()>MAX_LENGTH) return false;
		}
		return true;
	}


	/**
	 * The Class Names.
	 */
	public static class Names implements Serializable {

		private static final long serialVersionUID = 54672347123L;

		/** The name1. */
		String name1;

		/** The name2. */
		String name2;

		/** The name3. */
		String name3;

		/** The length. */
		int length = 0;


		/**
		 * Instantiates a new names.
		 */
		public Names() {
		}

		/**
		 * Sets the name1.
		 *
		 * @param name the new name1
		 */
		public void setName1(String name) {
			this.length = max(length, 1);
			this.name1 = name;
		}

		/**
		 * Sets the name2.
		 *
		 * @param name the new name2
		 */
		public void setName2(String name) {
			this.length = max(length, 2);
			this.name2 = name;
		}

		/**
		 * Sets the name3.
		 *
		 * @param name the new name3
		 */
		public void setName3(String name) {
			this.length = max(length, 3);
			this.name3 = name;
		}

		/**
		 * Sets the name.
		 *
		 * @param at the at
		 * @param name the name
		 */
		public void setName(int at, String name) {
			if (at<0 || at>2) return ;
			switch (at) {
			case 0:
				setName1(name);
				break;
			case 1:
				setName2(name);
				break;
			case 2:
				setName3(name);
				break;
			}
		}

		/**
		 * find Max value.
		 *
		 * @param v1 the v1
		 * @param v2 the v2
		 * @return the int
		 */
		private int max(int v1, int v2) {
			return v1 >= v2 ? v1 : v2;
		}

		/**
		 * checks if sting is empty or null.
		 *
		 * @param name the name
		 * @return true, if successful
		 */
		private boolean empty(String name) {
			return name == null || name.isEmpty();
		}

		/**
		 * Rebuild and shifts values if necessary.
		 * @return the names
		 */
		public Names rebuild() {

			if (length == 0) return this;

			if (length>=3 && empty(name2) && !empty(name3)) {
				name2 = name3;
				name3 = null;
			}
			if (length>=2 && empty(name1) && !empty(name2)) {
				name1 = name2;
				name2 = name3;
				name3 = null;
			}

			if (!empty(name3)) length = 3;
			else
			if (!empty(name2)) length = 2;
			else
			if (!empty(name1)) length = 1;

			return this;

		}

		/**
		 * Gets the name1.
		 * @return the name1
		 */
		public String getName1() {
			return name1;
		}

		/**
		 * Gets the name2.
		 * @return the name2
		 */
		public String getName2() {
			return name2;
		}

		/**
		 * Gets the name3.
		 * @return the name3
		 */
		public String getName3() {
			return name3;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("s[%s, %s, %s]", name1,name2, name3);
		}
	}

}
