package net.j7.commons.utils;

public final class UtilsHash {

	public static final long hashString(CharSequence str) {
	 	long hash = 0;

	 	if (str==null) return hash;

	 	//Prehash
	 	 for (int i = 0, l = str.length(); i < l; i++) {
    		hash += str.charAt(i);
    		hash += (hash << 10);
    		hash ^= (hash >> 6);
    	}

    	//Posthash
    	//step 1
    	hash += (hash << 3);
    	hash ^= (hash >> 11);
    	hash += (hash << 15);
    	//step 2
    	hash = rehash(hash);

    	return hash;

	}

	/**
	 * @param hash
	 * @return
	 */
	public static long rehash(long hash) {
    	hash ^= (hash >>> 20) ^ (hash >>> 12);
        hash = hash ^ (hash >>> 7) ^ (hash >>> 4);
        hash += ~(hash << 15);
        hash ^= (hash >>> 10);
        hash += (hash << 3);
        hash ^= (hash >>> 6);
        hash += ~(hash << 11);
        hash ^= (hash >>> 16);
		return hash;
	}

}
