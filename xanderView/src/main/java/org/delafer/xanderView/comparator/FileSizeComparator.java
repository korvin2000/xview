/**
 *
 */
package org.delafer.xanderView.comparator;

import net.j7.commons.strings.StringUtils;
import org.delafer.xanderView.file.entry.ImageAbstract;

import java.util.Comparator;

/**
 * @author tavrovsa
 *
 */
public class FileSizeComparator implements Comparator<ImageAbstract<?>> {


    /**  Lazy-loaded Singleton, by Bill Pugh **/
    private static class Holder {
        private final static Comparator<ImageAbstract<?>> INSTANCE = new FileSizeComparator();
    }

    public static Comparator<ImageAbstract<?>> instance() {
        return Holder.INSTANCE;
    }

	public FileSizeComparator() {
	}

	@Override
	public int compare(ImageAbstract<?> o1, ImageAbstract<?> o2) {
		return o1.size() > o2.size() ? 1 : o1.size() < o2.size() ? -1 : 0;
	}


}
