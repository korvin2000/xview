package net.j7.commons.ui;



import java.text.*;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import net.j7.commons.collections.Maps;
import net.j7.commons.strings.StringUtils;


/**
 * The Class CoreFormats
 *
 * @author  tavrovsa
 * @version $Revision: #1 $
 */
public class FormatUtils {


   private static final String PATTERN_CURRENCY_INT = "#,##0";

   private static final String PATTERN_CURRENCY_FLOAT = "#,##0.00";

   private static transient Map<Locale, FormatUtils> instances = Maps.newSynchronizedMap();

   private static Map<String, NumberFormat> currencies         = Maps.newSynchronizedMap();
   private static Map<String, NumberFormat> currenciesInt      = Maps.newSynchronizedMap();

   private java.util.Locale locale;
   private DecimalFormatSymbols symbols;

   private SimpleDateFormat dateFormat;
   private SimpleDateFormat dateTimeFormat;
   private SimpleDateFormat timeFormat;

   private NumberFormat  rateFormat;
   private NumberFormat  rateIntegerFormat;
   private NumberFormat  integerFormat;
   private NumberFormat  floatFormat;
   private NumberFormat  defaultFormat;

    public static final String CURRENCY_EURO = "\u20ac"; //"[$\u20AC-2]"; //Preffix
    public static final String CURRENCY_POUND = "\u00a3"; //"\243";
    public static final String CURRENCY_CENT = "\u00A2";
    public static final String CURRENCY_JAPANESE_YEN = "\u00A5";
    public static final String CURRENCY_DOLLAR = "$";

    final static Map<String, String> currencySymbs;
    static {
      currencySymbs = Maps.newSynchronizedMap(36);
      currencySymbs.put("EUR", CURRENCY_EURO);
      currencySymbs.put("GBP", CURRENCY_POUND);
      currencySymbs.put("CENT", CURRENCY_CENT);
      currencySymbs.put("JPY", CURRENCY_JAPANESE_YEN);
      currencySymbs.put("USD", CURRENCY_DOLLAR);
      currencySymbs.put("IEP", "IR\u00A3");
      currencySymbs.put("AED", "\u062F\u002e\u0625");
      currencySymbs.put("BDT", "\u09F3");
      currencySymbs.put("BRL", "R$");
      currencySymbs.put("CNY", "\u00a5");
      currencySymbs.put("CRC", "\u20a1");
      currencySymbs.put("CZK", "K\u010d");
      currencySymbs.put("DKK", "kr");
      currencySymbs.put("EGP", "\u00a3");
      currencySymbs.put("EUR", "\u20ac");
      currencySymbs.put("GBP", "\u00a3");
      currencySymbs.put("HRK", "kn");
      currencySymbs.put("HUF", "Ft");
      currencySymbs.put("IDR", "Rp");
      currencySymbs.put("ILS", "\u20AA");
      currencySymbs.put("INR", "Rs");
      currencySymbs.put("IQD", "\u0639\u062F");
      currencySymbs.put("ISK", "kr");
      currencySymbs.put("JPY", "\u00a5");
      currencySymbs.put("KRW", "\u20A9");
      currencySymbs.put("KWD", "\u062F\u002e\u0643");
      currencySymbs.put("LKR", "Rs");
      currencySymbs.put("LVL", "Ls");
      currencySymbs.put("MNT", "\u20AE");
      currencySymbs.put("MYR", "RM");
      currencySymbs.put("NOK", "kr");
      currencySymbs.put("PLN", "z\u0142");
      currencySymbs.put("RUB", "\u0440\u0443\u0431");
      currencySymbs.put("SAR", "\u0633\u002E\u0631");
      currencySymbs.put("THB", "\u0e3f");
      currencySymbs.put("VND", "\u20AB");
    }




   private FormatUtils(Locale locale) {
      this.locale = locale;
      this.symbols = new DecimalFormatSymbols(locale);
      initFormatters();
   }

   public static FormatUtils instance(Locale locale) {
      FormatUtils cf = instances.get(locale);
      if (null==cf) {
         cf = new FormatUtils(locale);
         instances.put(locale, cf);
      }
      return cf;
   }


   private void initFormatters() {
      //date formats
      dateFormat     = new SimpleDateFormat("dd.MM.yyyy", locale);
      dateTimeFormat    = new SimpleDateFormat("dd.MM.yyyy HH:mm", locale);
      timeFormat     = new SimpleDateFormat("HH:mm", locale);

      //numeric formats

      rateFormat     = new DecimalFormat("0.00%", symbols);
      //rateFormat         = NumberFormat.getPercentInstance(locale);
      rateIntegerFormat = new DecimalFormat("0%", symbols);

      integerFormat     = new DecimalFormat("0", symbols);
      //integerFormat      = NumberFormat.getIntegerInstance(locale);

      floatFormat    = new DecimalFormat("0.00", symbols);
      //floatFormat        = NumberFormat.getNumberInstance(locale);
      defaultFormat     = new DecimalFormat("#", symbols);


   }

   /* (non-Javadoc)
    * @see com.abovo.functional.document.IFormatters#date()
    */
   public SimpleDateFormat date() {
      return dateFormat;
   }

   /* (non-Javadoc)
    * @see com.abovo.functional.document.IFormatters#dateTime()
    */
   public SimpleDateFormat dateTime() {
      return dateTimeFormat;
   }

   /* (non-Javadoc)
    * @see com.abovo.functional.document.IFormatters#time()
    */
   public SimpleDateFormat time() {
      return timeFormat;
   }

   private static String currencySymbol(String currencyCode) {
      String code = currencySymbs.get(currencyCode);
      return code == null ? currencyCode : code;
   }

   public NumberFormat currency(String currencyCode) {
      NumberFormat res = currencies.get(currencyCode);

      if (null == res) {
         StringBuilder sb = new StringBuilder();
         sb.append(PATTERN_CURRENCY_FLOAT);
         sb.append(StringUtils.SPACE_CHAR);
         sb.append(currencySymbol(currencyCode));
         res = new DecimalFormat(sb.toString(), symbols);
         currencies.put(currencyCode, res);
      }

      return res;
   }

   public NumberFormat currencyInteger(String currencyCode) {
      NumberFormat res = currenciesInt.get(currencyCode);

      if (null == res) {
         StringBuilder sb = new StringBuilder();
         sb.append(PATTERN_CURRENCY_INT);
         sb.append(StringUtils.SPACE_CHAR);
         sb.append(currencySymbol(currencyCode));
         res = new DecimalFormat(sb.toString(), symbols);
         currenciesInt.put(currencyCode, res);
      }

      return res;
   }

   public Currency getCurrency(String currencyCode) {
      Currency cur = Currency.getInstance(currencyCode);

      return cur;
   }

   /* (non-Javadoc)
    * @see com.abovo.functional.document.IFormatters#percent()
    */
   public NumberFormat percent() {
      return rateFormat;
   }

   /* (non-Javadoc)
    * @see com.abovo.functional.document.IFormatters#percentInteger()
    */
   public NumberFormat percentInteger() {
      return rateIntegerFormat;
   }

   /* (non-Javadoc)
    * @see com.abovo.functional.document.IFormatters#integerFormat()
    */
   public NumberFormat integerFormat() {
      return integerFormat;
   }

   /* (non-Javadoc)
    * @see com.abovo.functional.document.IFormatters#floatFormat()
    */
   public NumberFormat floatFormat() {
      return floatFormat;
   }

   /* (non-Javadoc)
    * @see com.abovo.functional.document.IFormatters#defaultFormat()
    */
   public NumberFormat defaultFormat() {
      return defaultFormat;
   }

   public java.util.Locale getLocale() {
      return locale;
   }

}


