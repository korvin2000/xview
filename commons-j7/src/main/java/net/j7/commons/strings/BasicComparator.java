/**
 *
 */
package net.j7.commons.strings;

import java.util.Comparator;

/**
 * @author tavrovsa
 *
 */
public class BasicComparator implements Comparator<String> {


    /**  Lazy-loaded Singleton, by Bill Pugh **/
    private static class Holder {
        private final static Comparator<String> INSTANCE = new BasicComparator();
    }

    public static Comparator<String> instance() {
        return Holder.INSTANCE;
    }

	/**
	 *
	 */
	public BasicComparator() {
	}

	@Override
	public int compare(String o1, String o2) {
		return o1 == o2 ? 0 : o1 != null ? o1.compareToIgnoreCase(o2 != null ? o2 : StringUtils.EMPTY) : o2 != null ? -o2.length() : 0;
	}


}
