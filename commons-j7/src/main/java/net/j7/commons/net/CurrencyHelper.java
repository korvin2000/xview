package net.j7.commons.net;

import java.util.Hashtable;
import java.util.Map;

import net.j7.commons.strings.StringUtils;

public class CurrencyHelper {

    final static Map<String, String> currencySymbs;

    public static final String CURRENCY_EURO = "\u20ac"; //"[$\u20AC-2]"; //Preffix
    public static final String CURRENCY_POUND = "\u00a3"; //"\243";
    public static final String CURRENCY_CENT = "\u00A2";
    public static final String CURRENCY_JAPANESE_YEN = "\u00A5";
    public static final String CURRENCY_DOLLAR = "$";


    static {
    	currencySymbs = new Hashtable<String, String>(36);
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

	public static String currencySymbol(String currencyCode) {
		if (currencyCode==null) return StringUtils.EMPTY;
		String code = currencySymbs.get(currencyCode.toUpperCase());
		return code == null ? currencyCode : code;
	}


}
