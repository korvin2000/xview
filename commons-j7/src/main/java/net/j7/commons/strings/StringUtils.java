package net.j7.commons.strings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for any string manipulations.
 *
 * @author  tavrovsa
 * @version $Revision: #1 $ $Date: 2013/11/12 $
 */
public final class StringUtils {

   /** The technical logger to use. */

   /**
    * The default delimiter to be used to separate tokens. Example: abc, 1234, NULL
    */
   public static final String DEFAULT_TOKEN_DELIMITER = ",";

   /**
    * The default delimiter to be used to separate a part that should be appended to a string.
    *
    * @see #appendPart(StringBuffer, String, String)
    */
   public static final String DEFAULT_PART_DELIMITER = " ";

   /** Space Character */
   public final static transient char SPACE_CHAR = ' ';

   /** Empty String */
   public final static transient String EMPTY = "".intern();

   /** The Constant STRING_QUOTES. */
   public final static char[] STRING_QUOTES = new char[] { '\'', '"' };

   /** Space Character */
   public final static transient String SPACE = String.valueOf(SPACE_CHAR);

   public static final transient String LF = System.getProperty("line.separator");


   /** used in CP/M, MP/M, DOS, OS/2, Microsoft Windows, Symbian OS, Palm OS **/
   public static final transient String LF_DOS = "\r\n";

   /** used in (GNU/Linux, Google Android, AIX, Xenix, Mac OS X, FreeBSD, etc.), BeOS, Amiga, RISC OS **/
   public static final transient String LF_UNIX = "\n";

   /** Commodore machines, Apple II family, Mac OS up to version 9 and OS-9 **/
   public static final transient String LF_MAC9 = "\r";

   /** TAB constant **/
   public static final transient String TAB = "\t";

   private StringUtils() {

   }


   /**
    * StringUtils.toInteger("") = 0, StringUtils.toInteger(null) = null, StringUtils.toInteger("367 ") = 367,
    * StringUtils.toInteger("someTextHere") = null
    *
    * @param numericString
    * @return Integer value
    */
   public static Integer toInteger(String numericString) {

      if (numericString == null)
         return null;
      if (numericString.length() == 0)
         return Integer.valueOf(0);
      try {
         return Integer.parseInt(numericString.trim());
      } catch (Exception e) {
         return null;
      }

   }




   /**
    * This methods returns a string that contains stringified representation of the supplied <code>map</code>. The
    * string contains key=value pairs separated by <code>delimiter</code>. For example: from a map with key/values
    * 'a'='', 'b'='abc', 'c'='xyz' and delimiter ';' this method returns a string 'a=;b=abc;c=xyz'
    *
    * @param map Map to convert into a string.
    * @param delimiter The delimiter that separates token.
    * @return returns a string that contains stringified representation of the supplied <code>map</code> or empty string
    *         if the given map is null or empty.
    */
   public static String toPropertiesString(final Map map, final String delimiter) {

      final StringBuffer sb = new StringBuffer();

      if (map == null || map.isEmpty()) {
         return sb.toString();
      }

      final Set set = map.keySet();
      final Iterator itr = set.iterator();
      while (itr.hasNext()) {
         final Object key = itr.next();
         final Object value = map.get(key);

         if (key != null) {
            if (sb.length() > 0) {
               sb.append(delimiter);
            }
            sb.append(String.valueOf(key)).append("=");
            if (value != null) {
               sb.append(String.valueOf(value));
            }
         }
      }
      return sb.toString();
   }

   /**
    * This methods returns a string that contains stringified representation of the supplied <code>map</code>. The
    * string contains value separated by {@linkplain #DEFAULT_TOKEN_DELIMITER}; For example: from a map with key/values
    * 'a'='', 'b'='abc', 'c'='xyz' this method returns a string 'a=,b=abc,c=xyz'
    *
    * @param map Map to convert into a string.
    * @return returns a string that contains stringified representation of the supplied <code>map</code> or empty string
    *         if the given map is null or empty.
    */
   public static String toPropertiesString(final Map map) {

      return toPropertiesString(map, StringUtils.DEFAULT_TOKEN_DELIMITER);
   }


   /**
    * This methods returns a string that contains stringified representation of the supplied <code>list</code>. The
    * string contains value separated by <code>delimiter</code>. For example: from a list with values 'a', 'abc', 'xyz'
    * and delimiter ';' this method returns a string 'a;abc;xyz'
    *
    * @param list List to convert into a string.
    * @param delimiter The delimiter that separates token.
    * @return returns a string that contains stringified representation of the supplied <code>list</code> or empty
    *         string if the given list is null or empty.
    */
   public static String toListString(final List list, String delimiter) {

      final StringBuffer sb = new StringBuffer();

      if (list == null || list.isEmpty()) {
         return sb.toString();
      }

      for (int i = 0; i < list.size(); i++) {

         if (sb.length() > 0) {
            sb.append(delimiter);
         }
         sb.append(list.get(i));
      }
      return sb.toString();
   }

   /**
    * This methods returns a string that contains stringified representation of the supplied <code>list</code>. The
    * string contains value separated by {@linkplain #DEFAULT_TOKEN_DELIMITER}; For example: from a list with values
    * 'a', 'abc', 'xyz' this method returns a string 'a,abc,xyz'
    *
    * @param list List to convert into a string.
    * @return returns a string that contains stringified representation of the supplied <code>list</code> or empty
    *         string if the given list is null or empty.
    */
   public static String toListString(final List list) {

      return StringUtils.toListString(list, StringUtils.DEFAULT_TOKEN_DELIMITER);
   }

   /**
    * This methods returns a string that contains stringified representation of the supplied <code>set</code>. The
    * string contains value separated by <code>delimiter</code>. For example: from a set with values 'a', 'abc', 'xyz'
    * and delimiter ';' this method returns a string 'a;abc;xyz'
    *
    * @param set Set to convert into a string.
    * @param delimiter The delimiter that separates token.
    * @return returns a string that contains stringified representation of the supplied <code>set</code> or empty string
    *         if the given list is null or empty.
    */
   public static String toListString(final Set set, String delimiter) {

      return StringUtils.toListString(new ArrayList(set), delimiter);
   }

   /**
    * This methods returns a string that contains stringified representation of the supplied <code>set</code>. The
    * string contains value separated by {@linkplain #DEFAULT_PART_DELIMITER}; For example: from a set with values 'a',
    * 'abc', 'xyz' this method returns a string 'a,abc,xyz'
    *
    * @param set Set to convert into a string.
    * @param delimiter The delimiter that separates token.
    * @return returns a string that contains stringified representation of the supplied <code>set</code> or empty string
    *         if the given list is null or empty.
    */
   public static String toListString(final Set set) {

      return toListString(set, StringUtils.DEFAULT_PART_DELIMITER);
   }

   /**
    * Check if Object (String) is empty or null (leading and trailing whitespaces and<br>
    * other chars like linefeed chars, etc. are omitted/ignored, like it was done in<br>
    * String.trim() method )
    *
    * @param object
    * @return boolean, true if empty or null
    * @see empty
    */
   public static boolean isEmpty(final CharSequence s) {
	   int len;
		 if (null != s && (len = s.length())!=0)
		 while (len > 0) if (s.charAt(--len)>' ') return false;
		 return true;
   }

   /**
    * Check if Object (String) is empty or null (leading and trailing whitespace IS NOT omitted/ignored)
    *
    * @param object
    * @return boolean, true if empty or null
    * @see isEmpty
    */
   public final static boolean empty(final CharSequence s) {

      return null == s || 0 == s.length();
   }

   /**
    * Check if Object (String) is not empty.
    *
    * @param object
    * @return boolean, true if not empty
    */
	public final static boolean isNotEmpty(final CharSequence s) {
		 return !isEmpty(s);
		}

   /**
    * A replaceAll for StringBuffers. Replaces all occurrences of the search string with the replaceBy String. It even
    * works, when replacing the same String
    *
    * @param stringBuffer NOTE: This StringBuffer will be modified
    * @param search String to search (not an regular expression, simple Substring-match)
    * @param replaceBy will be inserted instead of the search string.
    * @throws IllegalArgumentException, when the search argument is an empty String
    */
   public static void replaceAll(StringBuffer stringBuffer, String search, String replaceBy)
         throws IllegalArgumentException {

      if (isEmpty(search)) {
         throw new IllegalArgumentException("Illegal Argument: search expression may not be an empty String");
      }

      int pos = -1;
      while ((pos = stringBuffer.indexOf(search, pos)) != -1) {
         stringBuffer = stringBuffer.replace(pos, pos + search.length(), replaceBy);
         pos = pos += replaceBy.length(); // advance past last replacement

      }
   }

   /**
    * This method appends the string <code>part</code> to the string buffer <code>sb</code>. If the StringBuffer is not
    * empty - the supplied <code>delimiter</code> will be append first and then the string <code>part</code>. Example1:
    * sb = "Lora" part="Kraft" delimiter="***" This method will return "Lora***Kraft". Example2: sb = "" part="Kraft"
    * delimiter="***" This method will return "Kraft". Example3: sb = "Lora" part="   " delimiter="***" This method will
    * return "Lora".
    *
    * @param sb The string buffer to append a part to.
    * @param part The string to be appended.
    * @param delimiter The delimiter to separate the content of the string buffer and part to be appended.
    * @return The modified string buffer <code>sb</code>.
    */
   public static StringBuilder appendPart(StringBuilder sb, final String part, final String delimiter) {

      if (part == null || part.trim().length() == 0) {
         return sb;
      }

      if (sb.length() > 0) {
         sb.append(delimiter);
      }

      sb.append(part.trim());

      return sb;
   }

   /**
    * This method appends the string <code>part</code> to the string buffer <code>sb</code>. This method calls the
    * {@linkplain #appendPart(StringBuffer, String, String)} with delimiter {@link #DEFAULT_PART_DELIMITER " "}. <li>
    * Example1: sb = "Lora" part="Kraft" This method will return "Lora Kraft". <li>Example2: sb = "" part="Kraft" This
    * method will return "Kraft". <li>Example3: sb = "Lora" part="  " This method will return "Lora".
    *
    * @param sb The string buffer to append a part to.
    * @param part The string to be appended.
    * @return The modified string buffer <code>sb</code>.
    * @see #appendPart(StringBuffer, String, String)
    */
   public static StringBuilder appendPart(StringBuilder sb, final String part) {

      return appendPart(sb, part, DEFAULT_PART_DELIMITER);
   }

   /**
    * Returns the supplied string <code>from</code> or an EMPTY string if the string <code>from</code> is NULL. This
    * method NEVER returns NULL.
    *
    * @param from The String to get the result from.
    * @return the supplied string <code>from</code> or an EMPTY string if the string <code>from</code> is NULL.
    */
   public static String getNotNull(String from) {

      if (from == null) {
         return "";
      }
      return from;
   }

   /**
    * returns "-" in case, object was empty.
    *
    * @param from
    * @return from.toString or "-"
    */
   public static String getContentOrDash(Object from) {
	  String str = null;
      if (null == from || StringUtils.isEmpty(str = from.toString())) {
         return "-";
      }
      return str;

   }

   public static String removeAllSpecialCharacter(String source) {

      return replaceAllSpecialCharacter(source, " ");
   }

   public static String replaceAllSpecialCharacter(String source, String replacement) {

      StringBuffer sb = new StringBuffer();
      if (source != null) {
         for (int index = 0; index < source.length(); index++) {
            char c = source.charAt(index);
            if (Character.isLetterOrDigit(c)) {
               sb.append(c);
            } else {
               sb.append(replacement);
            }
         }
      }
      return sb.toString();
   }

   /**
    * Returns a UPPER CASE copy of the given string <code>from</code> or NULL if the supplied parameter is NULL.
    *
    * @param from the string to get UPPER representation from.
    * @return a UPPER CASE copy of the given string <code>from</code> or NULL if the supplied parameter is NULL.
    */
   public static String toUpperCase(String from) {

      if (from == null) {
         return null;
      }

      return from.toUpperCase();
   }

   /**
    * Util method to get a proper not NULL string
    */
   public static String getStringValue(final Object o) {

      if (o instanceof String) {
         return (String) o;
      }

      String param = "";
      if (o != null) {
         param = o.toString();
      }

      return param;
   }

   /**
    * Example: You have a number = 1 and you want it to format it as String like "0001", then start the method with
    * fillUpValueWithCharacter(4, "0", 1);
    *
    * @param length
    * @param character
    * @param value
    * @return
    */
   public static String fillUpValueWithCharacter(int length, char character, Object value) {

      return fillUpValueWithCharacter(length, character, value.toString());
   }

   /**
    * Example: You have a number = 1 and you want it to format it as String like "0001", then start the method with
    * fillUpValueWithCharacter(4, "0", 1);
    *
    * @param length
    * @param character
    * @param value
    * @return
    */
   public static String fillUpValueWithCharacter(int length, char character, int value) {

      return fillUpValueWithCharacter(length, character, String.valueOf(value));
   }

   /**
    * Example: You have a number = 1 and you want it to format it as String like "0001", then start the method with
    * fillUpValueWithCharacter(4, "0", 1);
    *
    * @param length
    * @param character for filling up the value
    * @param value
    * @return value enriched brimful with characters
    */
   public static String fillUpValueWithCharacter(int length, char character, String value) {

      if (value.length() >= length) {
         return value.substring(0, length);
      }

      int characterCount = length - value.length();

      for (int x = 0; x < (characterCount); x++) {
         value = character + value;
      }

      return value;
   }

   public static String substring(String in, int maxlength) {

      if (in.length() > maxlength) {
         return in.substring(0, maxlength);
      }
      return in;
   }

   /**
    * Simple replace all occurrences in given source string with a new string value
    *
    * @param source the input string
    * @param replaceable string - find what
    * @param replacing string - replace with
    * @return the string
    */
   public static String replace(String source, String replaceFrom, String replaceTo) {

      if (null == source || null == replaceFrom || replaceFrom.length() == 0)
         return source;

      char ch;
      int corr = (replaceTo != null
            ? replaceTo.length()
            : 0) - replaceFrom.length() << 1;

      StringBuilder sb = new StringBuilder(source.length() + (corr > 0
            ? corr
            : 0));
      int eqPoz = 0;
      for (int i = 0; i < source.length(); i++) {
         ch = source.charAt(i);

         if (ch == replaceFrom.charAt(eqPoz)) {
            if (eqPoz == replaceFrom.length() - 1) {
               eqPoz = 0;
               sb.append(replaceTo);
            } else {
               eqPoz++;
               if (eqPoz >= replaceFrom.length())
                  eqPoz = 0;
            }

         } else if (eqPoz > 0) {
            sb.append(replaceFrom.substring(0, eqPoz));
            i--;
            eqPoz = 0;
         } else
            sb.append(ch);

      }
      if (eqPoz > 0)
         sb.append(replaceFrom.substring(0, eqPoz));
      return sb.toString();
   }

   /**
    * Replace all specified chars in the string with a new string value
    *
    * @param source the input string
    * @param fromStr - find what
    * @param toStr - replace with
    * @return the string
    */
   public final static String replace(String source, char fromStr, String toStr) {

      StringBuilder sb = new StringBuilder(source.length() + 4);
      char ch;
      for (int i = 0; i < source.length(); i++) {
         ch = source.charAt(i);
         sb.append(ch != fromStr
               ? ch
               : toStr);
      }
      return sb.toString();
   }

   /**
    * Fill string with given char to given length. Example: a = fillString("123",6,'0'); result> a = 000123
    *
    * @param str the string
    * @param length the length
    * @param toFill the char to fill
    * @return the string
    * @see fillStringRight
    */
   public final static String fillString(final String str, final int length, final char toFill) {

      if (null == str)
         return str;
      StringBuilder sb = new StringBuilder(length);
      final int difference = length - str.length();

      for (int i = difference; i > 0; i--)
         sb.append(toFill);
      sb.append(str);
      return sb.toString();
   }

   /**
    * Fill string with given char to given length. Example: a = fillStringRight("123",6,'x'); result> a = 123xxx
    *
    * @param str the string
    * @param length the length
    * @param toFill the char to fill
    * @return the string
    * @see fillString
    */
   public final static String fillStringRight(final String str, final int length, final char toFill) {

      if (null == str)
         return str;
      StringBuilder sb = new StringBuilder(length);
      final int difference = length - str.length();

      sb.append(str);
      for (int i = difference; i > 0; i--)
         sb.append(toFill);

      return sb.toString();
   }

   /**
    * Removes all spaces from String
    *
    * @param str - the input string
    * @return the string Input - "123 456 789" Ouput - "123456789"
    */
   public final static String removeSpaces(final String str) {

      if (null == str)
         return str;

      StringBuilder buf = new StringBuilder(str.length());
      char ch;

      for (int i = 0, length = str.length(); i < length; i++) {
         ch = str.charAt(i);
         if (ch != SPACE_CHAR)
            buf.append(ch);
      }

      return buf.toString();
   }

   /**
    * @see String shrink(String str, int length, String suffix) calls method shrinkStr(str, 64);
    */
   public static String shrink(String str) {

      return shrink(str, 64);
   }

   /**
    * @see String shrink(String str, int length, String suffix) calls method shrinkStr(str, length, "...");
    */
   public static String shrink(String str, int length) {

      return shrink(str, length, null);
   }

   /**
    * Shrink/Cut string to N chars and add given suffix to end of string
    *
    * @param str the string
    * @return cutted string
    */
   public static String shrink(String str, int length, String suffix) {

      if (str == null)
         return null;
      return (str.length() > length)
            ? suffix != null
                  ? str.substring(0, length) + suffix
                  : str.substring(0, length)
            : str;
   }

   /**
    * Concatenates given array of strings and separate them with divider eG. Input - concat(", ","yes","no","ignore") ->
    * Output - "yes, no, ignore"
    *
    * @param divider the divider
    * @param args the args
    * @return the string
    */
   public final static String concat(String divider, final CharSequence... args) {

      if (args == null)
         return EMPTY;
      if (divider == null)
         divider = SPACE;
      int i = 0;
      final int k = args.length - 1;
      int j = k * divider.length();
      for (i = k; i >= 0; i--)
         j += args[i] != null
               ? args[i].length()
               : 0;

      StringBuilder sb = new StringBuilder(j);

      for (i = 0; i <= k; i++)
         if (args[i] != null && args[i].length() > 0) {
            if (sb.length() > 0)
               sb.append(divider);
            sb.append(args[i]);
         }

      return sb.toString();
   }

   /**
    * First not empty string from array of strings
    *
    * @param args the args
    * @return the string
    */
   public static final String firstNotEmpty(String... args) {

      if (args != null)
         for (String text : args) {
            if (!isEmpty(text))
               return text;
         }

      return EMPTY;
   }

   /**
    * Checks if is digit / number
    *
    * @param char ch - single char
    * @return true, if is digit
    */
   public final static boolean isDigit(final char ch) {

      return ((ch >= '0') && (ch <= '9'));
   }

   /**
    * Compares this {@code String} to another {@code String}, ignoring case considerations. Two strings are considered
    * equal ignoring case if they are of the same length and corresponding characters in the two strings are equal
    * ignoring case.
    *
    * @param s1 the first string
    * @param s2 the second string
    * @return true, if strings are equal
    */
   public final static boolean compareIgnoreCase(String s1, String s2) {

      return s1 != null
            ? s1.equalsIgnoreCase(s2)
            : s2 == null;
   }

   /**
    * Compares two string with each other. The result is {@code true} if and only if the argument is not {@code null}
    * and is a {@code String} object that represents the same sequence of characters as this object.
    *
    * @param s1 the first string
    * @param s2 the second string
    * @return true, if strings are equal
    */
   public final static boolean compare(String s1, String s2) {

      return s1 != null
            ? s1.equals(s2)
            : s2 == null;
   }

   /**
    * Check if set of strings separated with "," or ";" contains given string value.
    *
    * @param strs the set of strings separated with "," or ";"
    * @param value the string value
    * @return true, if array of string contains given string value.
    */
   public final static int containsValue(final String strs, String value) {

      return containsValue(getStringsArray(strs), value);
   }

   /**
    * Check if array of string contains given string value.
    *
    * @param strs the String[] array
    * @param value the string value
    * @return true, if array of string contains given string value.
    */
   public final static int containsValue(final String[] strs, String value) {

      if (null != strs && null != value) {
         value = value.trim();

         for (int i = strs.length - 1; i >= 0; i--)
            if (strs[i].equalsIgnoreCase(value))
               return i;
      }

      return -1;
   }

   /**
    * Converts string with values separated with "," or ";" to String[] array.
    *
    * @param str the string
    * @return the strings array
    */
   public final static String[] getStringsArray(String str) {

      int count = 0;

      if (empty(str))
         return new String[count];

      str += ';';

      for (int i = 0; i < str.length(); i++) {
         char ch = str.charAt(i);
         if (ch == ';' || ch == ',')
            count++;
      }

      String[] strs = new String[count];
      StringBuilder sb = new StringBuilder();
      count = 0;

      for (int i = 0; i < str.length(); i++) {
         char ch = str.charAt(i);

         if (ch == ';' || ch == ',') {
            strs[count] = sb.toString().trim();
            sb.delete(0, sb.length());
            count++;
         } else
            sb.append(ch);
      }

      return strs;
   }

   /**
    * Checks if given array of chars (second argument) contains some char (first argument).
    *
    * @param ch the char
    * @param chars the chars
    * @return true, if successful
    */
   public final static boolean containsChar(final char ch, final char[] chars) {

      for (char element : chars)
         if (ch == element)
            return true;

      return false;
   }

   /**
    * Cut sequential spaces - replace sequences with 2 or more spaces to 1 space only (this method ignores text in
    * quotes "", '' )
    *
    * @param str the input string
    * @param strMarkers the str markers
    * @param cutAll the cut all
    * @return the string
    */
   public final static String cutSpaces(final String str, boolean cutAll) {

      if (null == str)
         return str;

      final char[] strMarkers = STRING_QUOTES;
      StringBuffer sb = new StringBuffer(str.length());
      char ch, oldCh = (char) 0;
      boolean subStr = false;

      for (int i = 0; i < str.length(); i++) {
         ch = str.charAt(i);
         if (containsChar(ch, strMarkers))
            subStr = false == subStr;
         if (!subStr && ch == SPACE_CHAR && ch == oldCh)
            continue;

         if (ch != SPACE_CHAR || !cutAll)
            sb.append(ch);

         oldCh = ch;
      }

      return sb.toString().trim();
   }

   public enum CharSet {

      SPACES(SPACE_CHAR),
      UNVISIBLE_CHARS(SPACE_CHAR, '\r', '\n', '\t'),
      LINE_FEED_CHARS('\r', '\n'),
      SPACE_AND_CRLF(' ', '\r', '\n'),
      TABS('\t'),
      SPACES_AND_TABS(' ', '\t'),
      TRIM_METHOD_CHARS((char) 0, (char) 1, (char) 2, (char) 3, (char) 4, (char) 5, (char) 6, (char) 7, (char) 8,
            (char) 9, (char) 10, (char) 11, (char) 12, (char) 13, (char) 14, (char) 15, (char) 16, (char) 17,
            (char) 18, (char) 19, (char) 20, (char) 21, (char) 22, (char) 23, (char) 24, (char) 25, (char) 26,
            (char) 27, (char) 28, (char) 29, (char) 30, (char) 31, SPACE_CHAR);

      private final HashSet<Character> charSet;

      private CharSet(char... chars) {

         charSet = new HashSet<Character>(chars.length, 0.85f);
         for (char c : chars)
            charSet.add(c);
      }

      public final boolean contains(char ch) {

         return charSet.contains(Character.valueOf(ch));
      }
   }

   /**
    * Works like a java trim method, but only on the end of string + trims symbols in a given charset @see enum CharSet
    * eG. for CharSet.UNVISIBLE_CHARS - it trims not only spaces, but also other unvisible chars, like tabs, linefeeds
    * and spaces
    *
    * @see enum StringUtils.CharSet
    * @param str the string to trim at the end
    * @param set the - CharSet with chars which are used to trimm a string
    * @return string trimmed at the end
    */
   public static String trimStringEnd(String str, CharSet set) {

      if (str == null)
         return str;
      if (set == null)
         set = CharSet.UNVISIBLE_CHARS;
      int to = str.length();
      for (int i = str.length() - 1; i >= 0; i--) {
         char ch = str.charAt(i);
         if (set.contains(ch))
            to--;
         else
            break;
      }
      if (to == str.length())
         return str;
      return str.substring(0, to);
   }

   /**
    * Works like a java trim method, but only on the beginning of string + trims symbols in a given charset @see enum
    * CharSet eG. for CharSet.UNVISIBLE_CHARS - it trims not only spaces, but also other unvisible chars, like tabs,
    * linefeeds and spaces
    *
    * @param str the string to trim at the beginning
    * @param set the - CharSet with chars which are used to trimm a string
    * @return string trimmed at the beginning
    * @see enum StringUtils.CharSet
    */
   public static String trimStringStart(String str, CharSet set) {

      if (str == null)
         return str;
      if (set == null)
         set = CharSet.UNVISIBLE_CHARS;
      int from = 0;
      for (int i = 0; i < str.length(); i++) {
         char ch = str.charAt(i);
         if (set.contains(ch))
            from++;
         else
            break;
      }
      if (from == 0)
         return str;
      return str.substring(from);
   }

   public static String mergeNotEmptyStrings(String separator, String... strings) {
	      return mergeNotEmptyStrings(false, separator, strings);
   }

   /**
    * Merge non empty strings, separated with given _seperator_, usually a space<br>
    * eG. sysout -> mergeNotEmptyStrings(" ", "Herr", null, "Michael", "Lehmann");<br>
    * result> "Herr Michael Lehmann"<br>
    * <br>
    *
    * @param separator
    * @param strings
    * @return
    */
   public static String mergeNotEmptyStrings(boolean doTrimm, String separator, String... strings) {

      if (strings == null || strings.length == 0) return EMPTY;

      StringBuilder sb = new StringBuilder();
      for (String next : strings) {
         if (isEmpty(next)) continue;
         if (sb.length() > 0) sb.append(separator);
         sb.append(doTrimm ? next.trim() : next);
      }
      return sb.toString();
   }

   /**
    * Eliminates LineFeed by replacing it with space " "
    *
    * @param text
    * @return clean string
    */
   public static String eliminateLineFeed(String text) {

      return eliminateLineFeed(text, " ");
   }

   /**
    * Eliminates LineFeed by replacing it with an placeHolder
    *
    * @param text
    * @param placeHolder
    * @return clean string
    */
   public static String eliminateLineFeed(String text, String placeHolder) {

      if (isEmpty(text))
         return text;

      return text.replaceAll("\\r\\n|\\r|\\n", placeHolder);
   }



   private static int min(int a1, int a2) {
	   return a1 < a2 ? a1 : a2;
   }

   public static String subStringLeft(String str, int length) {
	   if (str==null || str.isEmpty()) return str;
	   return str.substring(0, min(str.length(), length));
   }

   public static String subStringRight(String str, int length) {
	   if (str==null || str.isEmpty()) return str;
	   return str.substring(str.length() - min(str.length(), length));
   }



   public static boolean hasUpperLetter(final CharSequence s) {
	   int len;
		 if (null != s && (len = s.length())!=0)
		 while (len > 0) if (Character.isUpperCase(s.charAt(--len))) return true;
		 return false;
   }


   public static String toLowerCase(final String s) {
	   return hasUpperLetter(s) ? s.toLowerCase() : s;
   }

   public static boolean asBoolean(String str) {
		if (str == null || str.length()==0) return false;
		str = str.trim();
		if ("1".equals(str)) return true;
		str = str.toLowerCase();
		return "true".equals(str) || "yes".equals(str) || "y".equals(str) || "on".equals(str) || "ja".equals(str) || "enable".equals(str);
	}


   public static String[] splitValues(String values) {
	   String[] arr = values.split(",");
	   if (arr.length<=1) {
		   String[] tmp = values.split(";");
		   if (tmp.length>arr.length) arr = tmp;
	   }
	   return arr;
   }

}

