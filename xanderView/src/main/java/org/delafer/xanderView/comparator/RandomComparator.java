package org.delafer.xanderView.comparator;

import org.delafer.xanderView.file.entry.ImageAbstract;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class RandomComparator implements Comparator<ImageAbstract<?>> {

    public static Comparator<ImageAbstract<?>> instance() {
        return new RandomComparator();
    }


    private final transient Map<String, Long> stored = new TreeMap<>();

    private Random random;
    /**
     *
     */
    public RandomComparator() {
            random = getWeakRandom();
    }

	public static java.util.Random getWeakRandom() {
		return new Random(System.nanoTime());
	}

	public static java.util.Random getStrongRandom() {
		try {
			return SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			return getWeakRandom();
		}
	}

    @Override
    public int compare(ImageAbstract<?> o1, ImageAbstract<?> o2) {
        return getId(o1).compareTo(getId(o2));
    }

    private final Long getId(final ImageAbstract<?> ia) {
	    return stored.computeIfAbsent(ia.name(), k -> random.nextLong());
    }
}
