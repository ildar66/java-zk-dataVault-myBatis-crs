package ru.masterdm.crs.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Family of new collectors to use at stream collect.
 * @author Pavel Masalov
 */
public final class CollectorUtils {

    /**
     * Private constructor of type.
     */
    private CollectorUtils() {
    }

    /**
     * Collect elements into immutable list.
     * @param collectionFactory supply creator of collection
     * @param <T> the type of input elements to the reduction operation
     * @param <A> the result type of the reduction operation
     * @return the new {@code Collector}
     */
    public static <T, A extends List<T>> Collector<T, A, List<T>> toImmutableList(Supplier<A> collectionFactory) {
        return Collector.of(collectionFactory, List::add,
                            (left, right) -> {
                                left.addAll(right);
                                return left;
                            }, Collections::unmodifiableList);
    }

    /**
     * Collect elements into immutable array list.
     * @param <T> the type of input elements to the reduction operation
     * @return the new {@code Collector}
     */
    public static <T> Collector<T, List<T>, List<T>> toImmutableList() {
        return toImmutableList(ArrayList::new);
    }

    /**
     * @param <T> the type of input elements to the reduction operation
     * @return the new {@code Collector}
     */
    public static <T> Collector<T, ?, T> singletonCollector() {
        return Collectors.collectingAndThen(Collectors.toList(),
                                            list -> {
                                                if (list.size() > 1) {
                                                    throw new IllegalStateException("More then one element at list");
                                                } else if (list.size() == 0) {
                                                    return null;
                                                }
                                                return list.get(0);
                                            });
    }
}
