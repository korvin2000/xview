/**
 *
 */
package net.j7.commons.strings;

import java.util.Comparator;

/**
 * @author tavrovsa
 *
 */
public class BogusComparator implements Comparator<String> {


    /**  Lazy-loaded Singleton, by Bill Pugh **/
    private static class Holder {
        private final static Comparator<String> INSTANCE = new BogusComparator();
    }

    public static Comparator<String> instance() {
        return Holder.INSTANCE;
    }

	/**
	 *
	 */
	public BogusComparator() {
	}

	@Override
	public int compare(String o1, String o2) {
		return -1;
	}


}
