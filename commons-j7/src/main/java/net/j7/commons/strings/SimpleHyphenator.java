/*
 * @File: TextJoin.java
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The Class SimpleHyphenator / easy and simple line breaking algorithm<br>
 *
 * A hyphenation algorithm is a set of rules (especially one codified for implementation in a computer program) that<br>
 * decides at which points a word can be broken over two lines with a hyphen (punctuation mark). For example, a<br>
 * hyphenation algorithm might decide that impeachment can be broken as impeach-ment or im-peachment, but not,<br>
 * say, as impe-achment.<br>
 * <br>
 * In typography, the breaking of a word into syllables and inserting hyphens, manually or automatically, so that<br>
 * word spaces remain consistent�within prescribed limits�for proper justification.<br>
 *
 * There are several basic rules for hyphenation:<br>
 * <br>
 * 1. There must be at least two characters on both sides of the hyphen.<br>
 * 2. Numerals should not be hyphenated except, in an emergency, at a comma point.<br>
 * 3. It is not good practice to hyphenate in a headline.<br>
 * 4. A one-syllable word should never be hyphenated, though some systems will certainly try.
 * 5. A word should be divided on a double consonant, unless the word root ends with a double consonant (e.g., miss-ing, not mis-sing).<br>
 * 6. More than three hyphens should not be used in a row. Too many hyphens in a row (or too many hyphens in a text block) is referred to as pig bristles.<br>
 * <br>
 * An incorrect word division is called a bad break. For maximum legibility, hyphenation should be used as little as possible.<br>
 *
 * This is simply and dirty implementation of hyphenation algorithm for english and german languages without exception dictionary,<br>
 * so it could produce "bad break"'s in some cases. But in most cases ( about 98-99% ) it produces right results.<br>
 * For 100% correct hyphenation results you should use other most powerfull algorithm.<br>
 *
 * @author  tavrovsa
 * @version $Revision: #1 $
 */
public class SimpleHyphenator {


   /** The Constant CHECK_CRLF_2. */
   private static final String[] CHECK_CRLF_2 = new String[]{"\\r", "\\n", "\r\n"};

   /** The Constant CHECK_CRLF_1. */
   private static final String CHECK_CRLF_1 = "\\r\\n";

   /** The Constant SEARCH_DEPTH. */
   private static final int SEARCH_DEPTH = 10;

   /** The Constant UMLTS. */
   private static final String UMLTS = "\u00f6\u00e4\u00fc\u00d6\u00c4\u00dc\u00df";

   /**
    * The Enum BreakType.
    */
   public enum BreakType {HYPHEN, SPACE, BREAK, BREAK_BEFORE};

   /**
    * The Enum CharType.
    */
   public enum CharType {NUMBER, LETTER, SPACE, OTHER};

   /**
    * Checks if input char is a break char.
    *
    * @param ch the ch
    * @return the break type
    */
   private static BreakType isBreakChar(char ch) {

      if (ch==' ') return BreakType.SPACE;

      CharType type = getCharType(ch);
      boolean nonBrakingChar = CharType.LETTER.equals(type) || CharType.NUMBER.equals(type);
      if (nonBrakingChar) return null;

      if ( ch=='(' ||  ch=='[' || ch == '{' ) return BreakType.BREAK_BEFORE;

      if ( ch==')' || ch == ']' || ch == '}') return BreakType.BREAK;

      if (ch=='\'' || ch=='"') return null;

      return BreakType.BREAK;
   }



   /**
    * Safe method to get a character at a given point.
    *
    * @param ch the ch
    * @param pos the pos
    * @return the character
    */
   private final static Character sGet(CharSequence ch, int pos) {
      return pos >= 0 && pos < ch.length() ? ch.charAt(pos) : null;
   }


   /**
    * Calculates smart position for algorithm.
    *
    * @param pos the pos
    * @param j the j
    * @return the int
    */
   private static int calcPsition(int pos, int j) {
      if (j == 0) return pos;
      boolean odd = (j % 2)!=0; //1,3,5,7
      int rPos = ((j+1) / 2);
      return odd ? pos - rPos : pos + rPos;
   }

   /**
    * Gets the char at a given position / point.
    *
    * @param ch the ch
    * @param pos the pos
    * @param j the j
    * @return the char at
    */
   private static Character getCharAt(CharSequence ch, int pos, int j) {

      return sGet(ch, calcPsition(pos, j));

   }


   /**
    * Breaker.
    *
    * @param str the str
    * @param bi the bi
    * @return the char sequence
    */
   private static CharSequence breaker(StringBuilder str, BreakInfo bi) {
      CharSequence ret = null;
      switch (bi.type) {
      case SPACE:
         ret = str.substring(0, bi.pos);
         str.delete(0, bi.pos+1);
         break;
      case BREAK:
         ret = str.substring(0, bi.pos+1);
         str.delete(0, bi.pos+1);
         break;
      case BREAK_BEFORE:
         ret = str.substring(0, bi.pos);
         str.delete(0, bi.pos);
         break;
      case HYPHEN:
      default:
         ret = str.substring(0, bi.pos+1)+"-";
         str.delete(0, bi.pos+1);
         break;
      }
      return ret;
   }


   /**
    * Trim.
    *
    * @param in the in
    * @return the char sequence
    */
   private static CharSequence trim(CharSequence in) {
     return in;//TODO implement trim function
   }


   /**
    * Hyphen text.
    *
    * @param text the text
    * @param preferredLength the preferred length
    * @return the string
    */
   public static String hyphenText(String text, int preferredLength) {
         return hyphenText(text, preferredLength, "\r\n");
   }

   public static String hyphenText(String text, int preferredLength, String breakChar) {
      return hyphenText(text, preferredLength, breakChar, 0);
   }

   /**
    * Hyphen text.
    *
    * @param text the text
    * @param preferredLength the preferred length
    * @param breakChar the break char
    * @return the string
    */
   public static String hyphenText(String text, int preferredLength, String breakChar, int minLines) {

      if (minLines==0 && (text == null || text.length() <= preferredLength)) return text;

      LinkedList<CharSequence> rs = new LinkedList<CharSequence>();

      if (text.length() > preferredLength) {

      StringBuilder sb = new StringBuilder(text);
      SimpleHyphenator.joinText(sb, preferredLength, rs);
      } else {
         rs.add(text);
      }

      int extraLen = (text.length() / preferredLength) * breakChar.length();
      StringBuilder ret = new StringBuilder(text.length()+extraLen);


      int j  = 0;
      for (CharSequence charSequence : rs) {

         CharSequence append = trim(charSequence);
         if (append != null && append.length()!=0) {
            if ((j++)>0)ret.append(breakChar);
            ret.append(append);
         }

      }
      if (j < minLines) {
//         ret.append(breakChar);
         for (int i = j; i < minLines; i++) ret.append(breakChar).append(' ');
      }
      return ret.toString();
   }

//   public static void main(String[] args) {
//
//         String a = " I go to the school every day but today is holiday and i dont want to go to the school";
//         String r = hyphenText(a, 16, "\r\n", 8);
//         System.out.println("--------");
//         System.out.println(r);
//         System.out.println("--------");
//
//   }
   /**
    * Join text.
    *
    * @param str the str
    * @param length the length
    * @param chunks the chunks
    */
   private static void joinText(StringBuilder str, int length, List<CharSequence> chunks) {

      if (str==null || str.length()==0) return ;

      BreakInfo info = searchBreakCharM0(str, length);
      if (info == null) info = searchBreakCharM1(str, length);
      if (info == null) info = searchBreakCharM2(str, length);
      if (info == null) info = searchBreakCharM3(str, length);

      if (info != null) {
         CharSequence chunk = breaker(str, info);
         chunks.add(chunk);
      }


      joinText(str, length, chunks);
   }


   /**
    * Abs.
    *
    * @param value the value
    * @return the int
    */
   private static final int abs(int value) {
      return value < 0 ? -value : value;
   }

   /**
    * Find best div.
    *
    * @param info the info
    * @param wi the wi
    * @return the integer
    */
   private static Integer findBestDiv(AutoHyphenInfo info, WordInfo wi) {
      List<Integer> lst = info.getDivs();
      Integer best = null;
      Integer ret = null;
      for (Integer val : lst) {
         int diff = abs ( wi.pos - val.intValue() );
         if (best == null || diff < best.intValue()) {
            best = diff;
            ret = val.intValue();
         }
      }
      return ret;
   }


   /**
    * Gets the break type.
    *
    * @param str the str
    * @param pos the pos
    * @return the break type
    */
   private static BreakType getBreakType(CharSequence str, int pos) {
      Character leftCh = sGet(str, pos);
      Character rightCh = sGet(str, pos+1);
      if (leftCh==null || rightCh==null) return BreakType.BREAK;

      CharType leftCt = getCharType(leftCh);
      CharType rightCt = getCharType(rightCh);

      if (CharType.LETTER.equals(leftCt) && CharType.LETTER.equals(rightCt)) return BreakType.HYPHEN;
      if (CharType.NUMBER.equals(leftCt) && CharType.NUMBER.equals(rightCt)) return BreakType.HYPHEN;

      return BreakType.BREAK;

   }

   /**
    * Search break char m2.
    *
    * @param str the str
    * @param length the length
    * @return the break info
    */
   private static BreakInfo searchBreakCharM2(StringBuilder str, int length) {
      if (length > str.length()) {

         length = str.length();
         return new BreakInfo(BreakType.BREAK, length-1);

      } else {
         WordInfo wi = findWord(str, length-1);
         AutoHyphenInfo info = SimpleHyphenator.AutoHyphenation.autoHyphen(wi.word);

         Integer best = findBestDiv(info, wi);

         if (best == null) return null;
         int pz = (length-1) + best - wi.pos;
         return  new BreakInfo(getBreakType(str, pz), pz);
      }
   }


   /**
    * Search break char m3.
    *
    * @param str the str
    * @param length the length
    * @return the break info
    */
   private static BreakInfo searchBreakCharM3(StringBuilder str, int length) {
      if (length > str.length()) {
         length = str.length();
         return new BreakInfo(BreakType.BREAK, length-1);
      }
      int pos = length - 1;
      return new BreakInfo(getBreakType(str, pos), pos);
   }

   /**
    * Gets the char type.
    *
    * @param ch the ch
    * @return the char type
    */
   private static CharType getCharType(char ch) {
      if (ch==' ') return CharType.SPACE;
      if (ch>='0' && ch<='9') return CharType.NUMBER;
      char lowerCase = Character.toLowerCase(ch);
      if ((lowerCase>='a' && lowerCase<='z')|| (UMLTS.indexOf(ch)>=0)) return CharType.LETTER;
      return CharType.OTHER;
   }

   /**
    * Find word.
    *
    * @param str the str
    * @param posAbs the pos abs
    * @return the word info
    */
   private static WordInfo findWord(StringBuilder str, int posAbs) {


      StringBuilder word = new StringBuilder();


      char center = str.charAt(posAbs);

      WordInfo ret = new WordInfo(word, 0);

      CharType type = getCharType(center);
      word.append(center);

      if (CharType.LETTER.equals(type) || CharType.NUMBER.equals(type)) {
         //reward
         int p = posAbs;

         while ((--p) > 0) {

            char newCh = str.charAt(p);
            CharType newType = getCharType(newCh);
            if (!newType.equals(type)) break;
            word.insert(0, newCh);
         }

         ret.pos = -1 + posAbs - p;

         p = posAbs;
         //forward
         while ((++p) < str.length()) {
            char newCh = str.charAt(p);
            CharType newType = getCharType(newCh);
            if (!newType.equals(type)) break;
            word.append(newCh);
         }


      } else {
        // "ignore";
      }

      return ret;



   }


   /**
    * Search break char m0.
    *
    * @param str the str
    * @param length the length
    * @return the break info
    */
   private static BreakInfo searchBreakCharM0(StringBuilder str, int length) {
      int len = str.length()-1;
      int to = length <= len ? length : len;

      for (int i = 0; i <= to; i++) {
         int hasBreak = hasCRLF(str, i, CHECK_CRLF_1);
         if (hasBreak == 0) hasBreak = hasCRLF(str, i, CHECK_CRLF_2);
         if (hasBreak == 0) hasBreak = hasCRLF(str, i, '\r','\n');
         if (hasBreak != 0) {
            str.delete(i, i+hasBreak-1);
            return new BreakInfo(BreakType.BREAK, i);
         }
      }

      return null;


   }






   /**
    * Checks for crlf.
    *
    * @param str the str
    * @param i the i
    * @param c the c
    * @param value the value
    * @return the int
    */
   private final static int hasCRLF(StringBuilder str, int i, char c, char value) {
      return (str.charAt(i) == value) ? 1 : 0;
   }



   /**
    * Checks for crlf.
    *
    * @param str the str
    * @param i the i
    * @param check the check
    * @return the int
    */
   private final static int hasCRLF(StringBuilder str, int i, String[] check) {
      for (int j = 0; j < check.length; j++) {
         if (hasCRLF(str, i, check[j]) != 0) return check[j].length();
      }
      return 0;
   }



   /**
    * Checks for crlf.
    *
    * @param str the str
    * @param fromIndex the from index
    * @param value the value
    * @return the int
    */
   private final static int hasCRLF(StringBuilder str, final int fromIndex, String value) {
      final int vl = value.length();
      return str.length() - fromIndex < vl ? 0 : (value.contentEquals( str.subSequence(fromIndex, fromIndex +vl)) ? vl : 0);
   }





   /**
    * Search break char m1.
    *
    * @param str the str
    * @param length the length
    * @return the break info
    */
   private static BreakInfo searchBreakCharM1(StringBuilder str, int length) {
//      if (true) return null;
      int SEARCHLEN = SEARCH_DEPTH;
      for (int i = 0; i < SEARCHLEN; i++) {
         Character ccc = getCharAt(str, length, i);
         if (ccc==null) continue;
         BreakType bt = isBreakChar(ccc.charValue());
         if (bt != null) return new BreakInfo(bt, calcPsition(length, i));
      }
      return null;
   }

 /**
 * The Class WordInfo.
 */
protected static class WordInfo {

      /** The word. */
      public CharSequence word;

      /** The pos. */
      public int pos;

      /**
       * Instantiates a new word info.
       *
       * @param word the word
       * @param pos the pos
       */
      public WordInfo(CharSequence word, int pos) {
         this.word = word;
         this.pos = pos;
      }
   }

   /**
    * The Class BreakInfo.
    */
   protected static class BreakInfo {

      /** The type. */
      public BreakType type;

      /** The pos. */
      public int pos;

      /**
       * Instantiates a new break info.
       *
       * @param type the type
       * @param pos the pos
       */
      public BreakInfo(BreakType type, int pos) {
         this.type = type;
         this.pos = pos;
      }
   }

   /**
    * The Class AutoHyphenation.
    */
   public static class AutoHyphenation {

      /** Vowel's list. */
      public static final String VOWELS = "aeiouy\u00e4\u00fc\u00f6";

      /**
       * Is this a vowel character.
       *
       * @param c - charater
       * @return true - if is vowel
       */
      static boolean isVowel(char c) {
              return VOWELS.indexOf(Character.toLowerCase(c)) != -1;
      }

      /**
       * The basic method for automatic spelling/hyphenation.
       * Auto hyphen.
       *
       * @param text the text
       * @return the string
       */
      public static final AutoHyphenInfo autoHyphen(CharSequence text) {
              if (text.length() < 4)
                      return new AutoHyphenInfo(text);
              // create seeds
              List<String> seeds = new ArrayList<String>();
              boolean vowel;
              StringBuilder sb = new StringBuilder();
              for (int i = 0; i < text.length(); i++) {
                      char c = text.charAt(i);
                      vowel = isVowel(c);
                      sb.append(c);
                      if (vowel) {
                              int remaining = text.length() - i - 1;
                              if (remaining == 0) {
                                      ; // nothing
                              } else if (remaining == 1) {
//                                    if (!isVowel(text.charAt(i + 1))) {
                                              i++;
                                              sb.append(text.charAt(i));
//                                    }
                              } else if (remaining == 2) {
                                      if (!isVowel(text.charAt(i + 1)) && !isVowel(text.charAt(i + 2))) {
                                              i++;
                                              sb.append(text.charAt(i));
                                              i++;
                                              sb.append(text.charAt(i));
                                      }
                              } else {
                                      if (!isVowel(text.charAt(i + 1)) && !isVowel(text.charAt(i + 2)) && !isVowel(text.charAt(i + 3))) {
                                              i++;
                                              sb.append(text.charAt(i));
                                              i++;
                                              sb.append(text.charAt(i));
                                      } else if (!isVowel(text.charAt(i + 1)) && !isVowel(text.charAt(i + 2)) && isVowel(text.charAt(i + 3))) {
                                              i++;
                                              sb.append(text.charAt(i));
                                      }
                              }
                              seeds.add(sb.toString());
                              sb.delete(0, sb.length());
                      }
              }
              if (sb.length() > 0) {
                      seeds.add(sb.toString());
              }

              // join seeds
              StringBuilder full = new StringBuilder();
              for (int i = 0 ; i < seeds.size(); i++) {
                      String seed = seeds.get(i);
                      if (i > 0) full.append("-");
                      full.append(seed);
                      if (i == 0) {
                              if (seed.length() == 1 && seeds.size() > 1) {
                                      full.append(seeds.get(++i));
                              }
                      }
              }

              full = (replace(full, "e-i-", "ei-"));
              full = (replace(full, "s-ch", "sch-"));
              full = (replace(full, "sc-h", "sch-"));
              full = (replace(full, "ung-s", "ungs-"));

              return new AutoHyphenInfo(full);
      }

      /**
       * Replace.
       *
       * @param source the source
       * @param replaceFrom the replace from
       * @param replaceTo the replace to
       * @return the string builder
       */
      public static StringBuilder replace(StringBuilder source, String replaceFrom, String replaceTo)
      {
          if(source == null || replaceFrom == null || replaceFrom.length() == 0)
              return source;
          int corr = (replaceTo == null ? 0 : replaceTo.length()) - replaceFrom.length() << 1;
          StringBuilder sb = new StringBuilder(source.length() + (corr <= 0 ? 0 : corr));
          int eqPoz = 0;
          for(int i = 0; i < source.length(); i++)
          {
              char ch = source.charAt(i);
              if(ch == replaceFrom.charAt(eqPoz))
              {
                  if(eqPoz == replaceFrom.length() - 1)
                  {
                      eqPoz = 0;
                      sb.append(replaceTo);
                  } else
                  if(++eqPoz >= replaceFrom.length())
                      eqPoz = 0;
              } else
              if(eqPoz > 0)
              {
                  sb.append(replaceFrom.substring(0, eqPoz));
                  i--;
                  eqPoz = 0;
              } else
              {
                  sb.append(ch);
              }
          }

          if(eqPoz > 0)
              sb.append(replaceFrom.substring(0, eqPoz));
          return sb;
      }

   }

   /**
    * The Class AutoHyphenInfo.
    */
   public static class AutoHyphenInfo {

      /** The divs. */
      private List<Integer> divs;

      /** The original. */
      private CharSequence original;

      /** The trnc. */
      private CharSequence trnc;

      /**
       * Instantiates a new auto hyphen info.
       *
       * @param input the input
       */
      public AutoHyphenInfo(CharSequence input) {
         if (input==null) input = "";

         original = input;

         divs = new LinkedList<Integer>();
         int len = input.length();
         StringBuilder sb = new StringBuilder(len);

         char ch;
         for (int i = 0; i < len; i++) {
            ch = input.charAt(i);
            if (ch=='-') {
               //TODO ignore after fist and after last char
               divs.add(Integer.valueOf(sb.length()-1));
            } else {
               sb.append(ch);
            }
         }
         trnc = sb;
      }


      /**
       * Gets the divs.
       *
       * @return Returns the divs.
       */
      public List<Integer> getDivs() {

         return divs;
      }


      /* (non-Javadoc)
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString() {
         StringBuilder sb = new StringBuilder();

         for (Integer tmp : divs) {
            if (sb.length()>0) sb.append(",");
            sb.append(tmp);
         }

         sb.insert(0, ' ');
         sb.insert(0, original);
         sb.insert(0, '/');
         sb.insert(0, trnc);
         return sb.toString();
      }

   }

// /**
// * @param args
// */
//public static void main(String[] args) {
//
//
//
//
////   String umlts = "\u00f6\u00e4\u00fc\u00d6\u00c4\u00dc\u00df";
////   System.out.println(umlts.indexOf('�'));
////   System.out.println(umlts);
////   System.out.println( 4 / 2 );
////
////   String aa = "I use here the following convention. The file name specified in";
////   aa = "+-0123456789ABCDEFGXYZ";
////   for (int i = 0; i < 20; i++) {
////      System.out.print(getCharAt(aa, 5, i));
////   }
//
//   System.out.println("alex".substring(0, 2));
//   StringBuilder a = new StringBuilder();
//   a.append("");
//   a.delete(0, 3);
//   System.out.println(a);
//
//   String text = "Enables a dump of statistics. Note: If activated content is sent to System.out! Returns a hyphenation tree for a given language and country, with fallback from (lang,country) to (lang). rightMin the minimum number of characters after the hyphenation point";
//
//   text += "Robinson Crusoe, Sohn eines nach England ausgewanderten Bremer Kaufmanns mit dem urspr�nglichen Namen Kreutznaer, wird 1632 in York geboren. Sein Vater sch�rft dem jungen Robinson ein, er geh�re in den Mittelstand, und warnt ihn eindringlich davor, zur See zu gehen, dort w�rde er seinen Untergang finden. Robinson Crusoe missachtet diese Ermahnungen und wird auf einer seiner ersten Fahrten vor der K�ste Nordafrikas von arabischen Piraten �berfallen und versklavt. Erst nach zweij�hriger Gefangenschaft in der marokkanischen Hafenstadt Sal� gelingt ihm zusammen mit dem ebenfalls versklavten Jungen Xury die Flucht; beide segeln entlang der afrikanischen Atlantikk�ste nach S�den. Schlie�lich werden sie von einem portugiesischen Kapit�n auf hoher See aufgenommen. Er bringt sie �ber den Ozean nach Brasilien; Robinson verkauft Xury an den Kapit�n, l�sst sich von ihm aber schriftlich zusichern, dass Xury nach 10 Jahren Dienst, wenn er ein Christ geworden ist, die Freiheit erhalten soll.";
//   text += "In Brasilien kommt Robinson durch Geschick im Handel schnell zu Geld. Er erwirbt eine eigene Zuckerplantage und bewirtschaftet sie so gut er es vermag. Um schwarze Sklaven f�r seine und andere Plantagen aus Guinea zu holen, geht er wieder zur See. Auf dieser Fahrt erleidet er bei einem Sturm in der Karibik Schiffbruch, den er als einziges Mitglied der Besatzung �berlebt. Er strandet an einer abgelegenen Insel im M�ndungsgebiet des Orinoco. Crusoe kann an den folgenden Tagen mit einem selbstgebauten Flo� noch verschiedene Ausr�stungsgegenst�nde aus dem Schiffswrack retten, bevor er eines Morgens feststellen muss, dass es nach einem weiteren Sturm verschwunden ist.";
//   text += "Robinson baut sich eine kleine Festung, in deren Schutz er lebt. Er beginnt, Getreide anzubauen, zu jagen und Kleidung aus den Fellen wilder Ziegen herzustellen. Etwa am zw�lften Tag nach seiner Landung errichtet er ein gro�es Kreuz, in das er den 30. September 1659 als Datum seiner Ankunft auf der Insel einritzt, und beschlie�t, fortan jeden Tag eine Kerbe in das Kreuz zu ritzen. Auch f�hrt er ein Tagebuch, bis ihm schlie�lich die Tinte ausgeht. Seine Festung r�stet er mit vom Schiff geretteten Musketen aus. All dies tut er mit �u�erster Vorsicht, da er sich auf der Insel nicht sicher f�hlt.";
//
////   try {
////   String fileName  = "d:\\book.tst";
////
////   if (fileName==null || fileName.trim().length()==0 || (!new File(fileName).exists())) {
////      System.out.println("Please specify valid filename to read xml data");
////      System.exit(0);
////   };
////
////   byte[] buffer = new byte[(int) new File(fileName).length()];
////   FileInputStream f = new FileInputStream(fileName);
////   f.read(buffer);
////   text = new String(buffer);
////   f.close();
////
////} catch (Exception e) {
////   e.printStackTrace();
////}
//
//   System.out.println(">"+SimpleHyphenator.hyphenText("", 26, "\r\n"));
//   System.out.println(">"+SimpleHyphenator.hyphenText("a", 26, "\r\n"));
//   System.out.println(">"+SimpleHyphenator.hyphenText(".", 26, "\r\n"));
//   System.out.println(">"+SimpleHyphenator.hyphenText("-", 26, "\r\n"));
//   System.out.println(">"+SimpleHyphenator.hyphenText("\r\n", 26, "\r\n"));
//   System.out.println(">"+SimpleHyphenator.hyphenText("\r", 26, "\r\n"));
//   System.out.println(">"+SimpleHyphenator.hyphenText(null, 26, "\r\n"));
//   System.out.println(">"+SimpleHyphenator.hyphenText("\r\n-", 26, "\r\n"));
//   System.out.println(">"+SimpleHyphenator.hyphenText("\r\na", 26, "\r\n"));
//   System.out.println(">"+SimpleHyphenator.hyphenText("b\r\n", 26, "\r\n"));
//
//   String normal = SimpleHyphenator.hyphenText(text, 26, "<br>");
//   System.out.println(normal);
//
//
//}


}
