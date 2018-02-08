package ru.masterdm.crs.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Utility class with some collection processing routines.
 * @author Pavel Masalov
 */
public final class CollectionUtils {

    /**
     * Protected constructor.
     */
    private CollectionUtils() {

    }

    /**
     * Class repesent splitted collection.
     * @param <T> type that stored at collection
     */
    public static class Splitted<T> {

        private Collection<T> inCondition = new ArrayList<>();
        private Collection<T> others = new ArrayList<>();

        /**
         * Returns collection with elements matched condition.
         * @return collection of elements
         */
        public Collection<T> getInCondition() {
            return inCondition;
        }

        /**
         * Returns collection with elements matched split condition.
         * @return collection of elements
         */
        public Collection<T> getOthers() {
            return others;
        }
    }

    /**
     * Split collection elements into two collection.
     * @param list input collection
     * @param predicate condition
     * @param <T> type of collection elements
     * @return split structure
     */
    public static <T> Splitted<T> split(Collection<T> list, Predicate<? super T> predicate) {
        final Splitted<T> splitted = new Splitted<>();

        for (T elem : list) {
            if (predicate.test(elem)) {
                splitted.inCondition.add(elem);
            } else {
                splitted.others.add(elem);
            }
        }
        return splitted;
    }

    /**
     * Detect if this meta is one if pointed.
     * @param key array of required meta keys
     * @param keys array of required meta keys
     * @return true if this meta is one of keys
     */
    public static boolean contains(String key, String... keys) {
        return ArrayUtils.contains(keys, key);
    }
}
