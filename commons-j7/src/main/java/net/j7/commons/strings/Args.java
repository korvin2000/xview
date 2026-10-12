/*
 * @File: Args.java
 *
 * 
 * 
 * All rights reserved.
 *
 * @Author:  tavrovsa
 *
 * @Version $Revision: #1 $Date: $
 *
 *
 */
package net.j7.commons.strings;

/**
 * Class Args. This is a very lightweight alternative to build-in java String format function, with some extra
 * functionality. [String.format("Name=%s, Phone=%s", "Albert Einstein", "+123-55-66-77")] Example:
 * Args.fill("%3 %2 = %1, %3 %4 = %1", "false","isCompany","boolean", "readOnly") =
 * "boolean isCompany = false, boolean readOnly = false"
 *
 * @author  tavrovsa
 * @version $Revision: #1 $
 * @category Parametric string functionality
 * @see java alternative - Strings.format(" %s... ", 0001);
 */
public class Args {

   private static final char NINE = '9';

   private static final char ZERO = '0';

   private static final int numeric(char ch) {

      return (byte) ch - (byte) ZERO;
   }

   /**
    * Fills string with arguments use "\" (Escape character) for special chars eG.
    * Arguments.string("Name: %1, Date: %3, Price: %2","Router",150d,new Date()); will return:
    * "Name: Router, Date <date>, Price: 150,00"
    *
    * @param source the source string
    * @param args the list of arguments passed to input string
    * @return the string
    */
   public final static String fill(String src, Object... args) {

      if (src == null || args == null || src.length() == 0 || args.length == 0)
         return src;

      StringBuilder sb = new StringBuilder(src.length() + (args.length * 6));

      char ch;
      boolean esc = false;
      boolean num = false;
      int no = 0;
      for (int i = 0; i < src.length(); i++) {
         ch = src.charAt(i);

         if (esc) {
            sb.append(ch);
            esc = false;
         } else {
            if (ch == '\\')
               esc = true;
            else if (ch == '%') {
               flush(sb, num, no, args);
               num = true;
               no = 0;
            } else if (num) {
               if (ch >= ZERO && ch <= NINE) {
                  no = no * 10 + numeric(ch);
               } else {
                  flush(sb, num, no, args);
                  sb.append(ch);
                  num = false;
               }

            } else
               sb.append(ch);
         }
      }

      flush(sb, num, no, args);

      return sb.toString();
   }

   /**
    * @param sb
    * @param num
    * @param no
    * @param args
    */
   private static void flush(StringBuilder sb, boolean num, int no, Object[] args) {

      if (num)
         if (no > 0)
            sb.append(get(args, no));
         else
            sb.append('%');
   }

   private static String get(Object[] args, int no) {

      if (no > args.length)
         return "";
      final Object r = args[no - 1];
      return r != null
            ? r.toString()
            : null;

   }

   // public static void main(String[] args) {
   //
   // System.out.println(Args.fill("%3 %2 = %1, %3 %4 = %1", "false", "isCompany", "boolean", "readOnly"));
   // System.out.println(fill("%11%1 %2 %3 %4 %5 %6%7%8%9%10%10%10", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
   // System.out.println(fill("%1x%5", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
   // System.out.println(fill("%1\\1", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
   // System.out.println(fill("%1%%%3%", 1, 2, "", 4, 5));
   // System.out.println(fill("%%%1%%", 1, 2, "", 4, 5));
   // System.out.println(fill("hight!", 1, 2, "", 4, 5));
   // System.out.println(fill("%1 %2 %3", null, null));
   // System.out.println(fill("privet(%1) masha - %2,%3", "Sasha", 12, 99));
   // }
}
