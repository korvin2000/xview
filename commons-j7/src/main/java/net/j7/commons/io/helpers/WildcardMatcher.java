package net.j7.commons.io.helpers;

import java.util.ArrayList;

import net.j7.commons.collections.FastStack;

public class WildcardMatcher {

	   private static final char ZV = '*';

	   private static final String QUEST = "?".intern();

	   private static final String STAR = "*".intern();


	   private static boolean hasWildcard(final CharSequence s) {
		   int len;
			 if (null != s && (len = s.length())!=0)
			 while (len > 0) if (s.charAt(--len)==ZV) return true;
			 return false;
	   }

	   private final static boolean equal(String o1, String o2)
	   {
	      return o1==o2 ? true : o1 != null ? o1.equalsIgnoreCase(o2) : false;
	   }

	   public static boolean isEqual(String testString, String wildcard, boolean fullCheck) {
		   return hasWildcard(wildcard) ? wildcardMatch(testString, wildcard) : fullCheck ? equal(testString, wildcard) : false;
	   }

	   public static boolean wildcardMatch(String testString, String wildcard) {
		   return wildcardMatch(testString, wildcard, false);
	   }

	   /**
	    * Checks a filename to see if it matches the specified wildcard matcher allowing control over case-sensitivity.
	    * <p>
	    * The wildcard matcher uses the characters '?' and '*' to represent a single or multiple wildcard characters. N.B. the sequence "*?" does not work properly
	    * at present in match strings.
	    *
	    *
	    * @param testString the filename to match on
	    * @param wildcard the wildcard string to match against
	    * @param caseSensitive what case sensitivity rule to use, null means case-sensitive
	    * @return true if the filename matches the wilcard string
	    *
	    */
	   public static boolean wildcardMatch(String testString, String wildcard, boolean caseSensitive) {
	      if (testString == null && wildcard == null) {
	         return true;
	      }
	      if (testString == null || wildcard == null) {
	         return false;
	      }
	      String[] wcs = tokenSplitter(wildcard);
	      boolean anyChars = false;
	      int textIdx = 0;
	      int wcsIdx = 0;
	      FastStack<int[]> backtrack = new FastStack<int[]>(6);
	      // loop around a backtrack stack, to handle complex * matching
	      do {
	         if (backtrack.size() > 0) {
	            int[] array = backtrack.pop();
	            wcsIdx = array[0];
	            textIdx = array[1];
	            anyChars = true;
	         }
	         // loop whilst tokens and text left to process
	         while (wcsIdx < wcs.length) {
	            if (wcs[wcsIdx].equals(QUEST)) {
	               // ? so move to next text char
	               textIdx++;
	               if (textIdx > testString.length()) {
	                  break;
	               }
	               anyChars = false;
	            } else if (wcs[wcsIdx].equals(STAR)) {
	               // set any chars status
	               anyChars = true;
	               if (wcsIdx == wcs.length - 1) {
	                  textIdx = testString.length();
	               }
	            } else {
	               // matching text token
	               if (anyChars) {
	                  //if any chars -> try to locate text token
	                  textIdx = checkIndexOf(testString, textIdx, wcs[wcsIdx], caseSensitive);
	                  if (textIdx == -1) {
	                     // token not found
	                     break;
	                  }
	                  int repeat = checkIndexOf(testString, textIdx + 1, wcs[wcsIdx], caseSensitive);
	                  if (repeat >= 0) {
	                     backtrack.push(new int[]{wcsIdx, repeat});
	                  }
	               } else // matching from current position
	               if (!isRegionMatches(testString, textIdx, wcs[wcsIdx], caseSensitive)) {
	                  // couldn't match token
	                  break;
	               }
	               // matched text token, move text index to end of matched token
	               textIdx += wcs[wcsIdx].length();
	               anyChars = false;
	            }
	            wcsIdx++;
	         }
	         // full match
	         if (wcsIdx == wcs.length && textIdx == testString.length()) {
	            return true;
	         }
	      } while (backtrack.size() > 0);
	      return false;
	   }

	   /**
	    * Checks if one string contains another starting at a specific index using the case-sensitivity rule.
	    * <p>
	    * This method mimics parts of {@link String#indexOf(String, int)} but takes case-sensitivity into account.
	    *
	    * @param str the string to check, not null
	    * @param strStartIndex the index to start at in str
	    * @param search the start to search for, not null
	    * @return the first index of the search String, -1 if no match or {@code null} string input
	    * @throws NullPointerException if either string is null
	    */
	   private static int checkIndexOf(String str, int strStartIndex, String search, boolean sensitive) {
	      int endIndex = str.length() - search.length();
	      if (endIndex >= strStartIndex) {
	         for (int i = strStartIndex; i <= endIndex; i++) {
	            if (isRegionMatches(str, i, search, sensitive)) {
	               return i;
	            }
	         }
	      }
	      return -1;
	   }

	   /**
	    * Checks if one string contains another at a specific index using the case-sensitivity rule.
	    * <p>
	    * This method mimics parts of {@link String#regionMatches(boolean, int, String, int, int)} but takes case-sensitivity into account.
	    *
	    * @param str the string to check, not null
	    * @param strStartIndex the index to start at in str
	    * @param search the start to search for, not null
	    * @return true if equal using the case rules
	    * @throws NullPointerException if either string is null
	    */
	   private static boolean isRegionMatches(String str, int strStartIndex, String search, boolean sensitive) {
	      return str.regionMatches(!sensitive, strStartIndex, search, 0, search.length());
	   }

	   /**
	    * Splits a string into a number of tokens. The text is split by '?' and '*'. Where multiple '*' occur consecutively they are collapsed into a single '*'.
	    *
	    * @param txt the text to split
	    * @return the array of tokens, never null
	    */
	   private static String[] tokenSplitter(String txt) {
	      // used by wildcardMatch
	      // package level so a unit test may run on this
	      if (txt.indexOf('?') == -1 && txt.indexOf('*') == -1) {
	         return new String[]{txt};
	      }
	      char[] array = txt.toCharArray();
	      ArrayList<String> list = new ArrayList<String>();
	      StringBuilder sb = new StringBuilder(txt.length());
	      for (int i = 0; i < array.length; i++) {
	         if (array[i] == '?' || array[i] == '*') {
	            if (sb.length() != 0) {
	               list.add(sb.toString());
	               sb.setLength(0);
	            }
	            if (array[i] == '?') {
	               list.add(QUEST);
	            } else if (list.isEmpty() || i > 0 && list.get(list.size() - 1).equals(STAR) == false) {
	               list.add(STAR);
	            }
	         } else {
	            sb.append(array[i]);
	         }
	      }
	      if (sb.length() != 0) {
	         list.add(sb.toString());
	      }
	      return list.toArray(new String[list.size()]);
	   }

}
