/**
 *
 */
package org.delafer.xanderView.comparator;

import org.delafer.xanderView.file.entry.ImageAbstract;

import java.io.IOException;
import java.util.Comparator;

/**
 * @author tavrovsa
 *
 */
public class FileDateComparator implements Comparator<ImageAbstract<?>> {


    /**  Lazy-loaded Singleton, by Bill Pugh **/
    private static class Holder {
        private final static Comparator<ImageAbstract<?>> INSTANCE = new FileDateComparator();
    }

    public static Comparator<ImageAbstract<?>> instance() {
        return Holder.INSTANCE;
    }

	public FileDateComparator() {
	}

	@Override
	public int compare(ImageAbstract<?> o1, ImageAbstract<?> o2) {
		try {
			long t1 = o1 != null ? o1.lastModified() : 0;
			long t2 = o2 != null ? o2.lastModified() : 0;
			return t1 > t2 ? 1 : t1 < t2 ? -1 : 0;
		} catch (IOException ignore) {
			return 0;
		}

	}


}
