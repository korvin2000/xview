package net.j7.commons.utils;



import java.math.BigDecimal;

public final class MathUtils {

	public static double min(double d1, double d2) {
		return d1 < d2 ? d1 : d2;
	}

	public static double min(double d1, double d2, double d3) {
		return min(min(d1, d2),d3);
	}

	public static double max(double d1, double d2) {
		return d1 > d2 ? d1 : d2;
	}

	public static double positive(double d1) {
		return d1 < 0d ? 0d : d1;
	}

	public static double abs(double d1) {
		return d1 < 0d ? -d1 : d1;
	}

	public static int abs(int d1) {
		return d1 < 0 ? -d1 : d1;
	}

	public static long intInLong(int a1, int a2) {
		return ((long)a1 << 32l) + (long)a2;
	}

	public static int[] longInInt(long l) {
		int[] ret = new int [2];
		long l1 = l >> 32l;
		ret[0] = (int)(l1);
		ret[1] = (int)(l - (l1 << 32l));
		return ret;
	}

	public static double round(double val) {
		return round(val, 2);
	}

	public static double multiRound(double val) {
		return multiRound(val, 2);
	}

	public static double round(double val, int precision)
	{
		if (! Double.isNaN(val))
		{
			boolean negative = val < 0d;

			BigDecimal db = BigDecimal.valueOf(negative ? -val : val);
			db = db.setScale(precision, BigDecimal.ROUND_HALF_UP);

			return negative ? -db.doubleValue() : db.doubleValue();
		}

		return 0d;
	}

	public static double multiRound(double val, int precision)
	{
		if (! Double.isNaN(val))
		{
			boolean negative = val < 0d;

			BigDecimal db = BigDecimal.valueOf(negative ? -val : val);
			db = db.setScale(precision, BigDecimal.ROUND_HALF_EVEN);

			return negative ? -db.doubleValue() : db.doubleValue();
		}

		return 0d;
	}

	/**
	 * Convert value GROSS to value NET
	 * 119 EUR - 19% (brutto) = 100 EUR (netto)
	 * pre-tax -> after tax
	 * @param gross value
	 * @param value added tax in procent (%)
	 *
	 * @return net value
	 * eG. 100,0 = grossToNet(119, 19)
	 */
	public static double grossToNet(double brutto, double rate) {
		return round( round(brutto) / (1.0d + (round(rate)/100d)) );
	}

	/**
	 * Convert value NET to value GROSS
	 * 100 EUR + 19% (netto) = 119 EUR (brutto)
	 * after tax -> pre-tax
	 * @param net value
	 * @param value added tax in procent (%)
	 *
	 * @return gross value
	 * eG. 119,0 = netToGross(100, 19)
	 */
	public static double netToGross(double netto, double rate) {
		return round( (1d + (round (rate)/100d)) * round(netto) );
	}


	/**
	 * Gets the absolute value added tax based on NET value
	 * e.G. 19% VAT from 100,- EUR NET =19,- EUR
	 *
	 * @param net the netto(after tax) value
	 * @param rate the rate in %
	 *
	 * @return absolute VAT value
	 */
	public static double getVATonNet(double netto, double rate) {
		return round ( (0.01d * round (rate)) * round(netto) );
	}


	/**
	 * Gets the absolute value added tax based on GROSS value
	 * e.G. 19% VAT from 119,-EUR GROSS = 19,- EUR
	 *
	 * @param gross the gross(pre-tax) value
	 * @param rate the rate in %
	 *
	 * @return absolute VAT value
	 */
	public static double getVATonGross(double gross, double rate) {
		//double t = round(rate) / 100d;
		//return multiRound(gross) * (t / (t + 1d));

		return round ( round(gross) * (1d - (1d / (1d + (round(rate)/100d)))) ) ;
	}


}
