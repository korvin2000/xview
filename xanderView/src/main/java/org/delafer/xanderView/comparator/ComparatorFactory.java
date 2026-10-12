package org.delafer.xanderView.comparator;

import org.delafer.xanderView.file.entry.ImageAbstract;
import org.delafer.xanderView.gui.config.ApplConfiguration;

import java.util.Comparator;

import static org.delafer.xanderView.gui.config.ApplConfiguration.SortType.UNSORTED;


public class ComparatorFactory {


	public static Comparator<ImageAbstract<?>> getComparator() {
		ApplConfiguration.SortBy sort = ApplConfiguration.instance().getSort();
		Comparator<ImageAbstract<?>> comparator = getBaseComparator(sort.sort);
		return null == comparator || sort.isAsc() ? comparator : comparator.reversed();
	}

	private static Comparator<ImageAbstract<?>> getBaseComparator(ApplConfiguration.SortType sort) {
		switch (sort) {
			case NAME:
				return BasicFileComparator.instance();
			case DATE:
			case DATETIME:
			case TIME:
				return FileDateComparator.instance();
			case SIZE:
				return FileSizeComparator.instance();
			case RANDOM:
				return RandomComparator.instance();
			case NATURAL:
				return AlphanumComparator.instance();
			case UNSORTED:
			case NO:
			default:
				return null;
		}
	}

}
