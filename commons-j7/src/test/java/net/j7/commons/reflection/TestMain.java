package net.j7.commons.reflection;

public class TestMain {

	public static void main(String[] args) {
		String a = "abc";
		String b = "abc";
		if (a.intern()==b.intern()) {
			System.out.println("they are the same");
		}

	}

}
