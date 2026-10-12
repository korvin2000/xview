package net.j7.commons.strings;

import java.util.Comparator;

public class Comparators {

	public static Comparator<String> getDummyComparator() {
		return BogusComparator.instance();
	}

	public static Comparator<String> getBasicComparator() {
		return BasicComparator.instance();
	}

	public static Comparator<String> getNaturalOrderComparator() {
		return NaturalOrderComparator.instance();
	}

}
