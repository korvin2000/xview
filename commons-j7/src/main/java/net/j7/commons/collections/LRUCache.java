/*
 * @File: LRUCache.java
 *
 * 
 * 
 * All rights reserved.
 *
 * @author  tavrovsa
 *
 * @Version $Revision: #4 $Date: $
 *
 *
 */
package net.j7.commons.collections;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Alexander Tawrowski
 * @version $Revision: #4 $ $Date: 2012/03/19 $
 */
public class LRUCache<K,V> implements Serializable {


   /**
    * Comment for <code>serialVersionUID</code>
    */
   private static final long serialVersionUID = 3328139471964580412L;


   public static final int DEFAULT_MAX_SIZE = 1000;

   private final Map<K,V> internalMap;

   private int maxSize;

   public LRUCache() {

      this(DEFAULT_MAX_SIZE);
   }

   /**
    * @param maxSize
    */
   public LRUCache(final int maxCacheSize) {
      this.maxSize = maxCacheSize;
      this.internalMap = Collections.synchronizedMap(new LinkedHashMap<K,V>(maxSize + 1, .75F, true) {

         /**
          * Comment for <code>serialVersionUID</code>
          */
         private static final long serialVersionUID = -340863866386824411L;

         protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {

            return size() > maxSize;
         }
      });
   }

   /**
    * @see Map#put(Object, Object)
    */
   public V put(K key, V value) {

      return internalMap.put(key, value);
   }

   /**
    * @see Map#get(Object)
    */
   public V get(Object key) {

      return internalMap.get(key);
   }

   /**
    * @see Map#remove(Object)
    */
   public V remove(Object key) {

      return internalMap.remove(key);
   }

   /**
    * @return
    * @see java.util.Map#keySet()
    */
   public Set<K> keySet(){

      return new HashSet<K>(internalMap.keySet());
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      return internalMap.hashCode();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @SuppressWarnings("rawtypes")
   @Override
   public boolean equals(Object obj) {

      if (this == obj) return true;
      if (!(obj instanceof LRUCache)) return false;

      LRUCache other = (LRUCache) obj;
      if (internalMap == null) return internalMap == other.internalMap;
      return internalMap.equals(other.internalMap);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      final int maxLen = 10;
      StringBuilder builder = new StringBuilder();
      builder.append("LRUCache [");
      builder.append("size=").append(internalMap.size()).append('/').append(this.maxSize);
      builder.append(", content=");
      builder.append(toString(internalMap.keySet(), maxLen));
      builder.append(']');
      return builder.toString();
   }

   private String toString(Set<K> collection, int maxLen) {

      StringBuilder builder = new StringBuilder();
      builder.append("[");
      int i = 0;
      for (Object object : collection) {
         if (i++ > 0) builder.append(", ");
         builder.append(object);
      }
      builder.append("]");
      return builder.toString();
   }
}
