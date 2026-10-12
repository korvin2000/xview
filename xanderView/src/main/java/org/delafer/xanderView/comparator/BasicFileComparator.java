/**
 *
 */
package org.delafer.xanderView.comparator;

import java.util.Comparator;

import net.j7.commons.strings.StringUtils;

import org.delafer.xanderView.file.entry.ImageAbstract;

/**
 * @author tavrovsa
 *
 */
public class BasicFileComparator implements Comparator<ImageAbstract<?>> {


    /**  Lazy-loaded Singleton, by Bill Pugh **/
    private static class Holder {
        private final static Comparator<ImageAbstract<?>> INSTANCE = new BasicFileComparator();
    }

    public static Comparator<ImageAbstract<?>> instance() {
        return Holder.INSTANCE;
    }

	/**
	 *
	 */
	public BasicFileComparator() {
	}

	public int cmp(String o1, String o2) {
		return o1 == o2 ? 0 : o1 != null ? o1.compareToIgnoreCase(o2 != null ? o2 : StringUtils.EMPTY) : o2 != null ? -o2.length() : 0;
	}

	private String asString(ImageAbstract<?> entry) {
		return entry != null ? entry.name() : null;
	}

	@Override
	public int compare(ImageAbstract<?> o1, ImageAbstract<?> o2) {
		return cmp(asString(o1), asString(o2));
	}


}
