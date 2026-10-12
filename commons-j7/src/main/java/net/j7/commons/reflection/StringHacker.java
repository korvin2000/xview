package net.j7.commons.reflection;


import java.lang.reflect.Field;
import java.util.Arrays;

public class StringHacker {


	private static final String EMPTY_CONSTANT = "x";

  public static void main(String[] args) throws SecurityException, NoSuchFieldException,
                          IllegalArgumentException, IllegalAccessException {


    Class<?> myClass = "x".intern().getClass();

    Field ms = myClass.getDeclaredField("value");
    ms.setAccessible(true);

    Object obj = ms.get("x".intern());
        char[] myChars = (char[]) obj;
        Arrays.fill(myChars, 0, myChars.length, 'X');

        System.out.println(EMPTY_CONSTANT);

  }
}
