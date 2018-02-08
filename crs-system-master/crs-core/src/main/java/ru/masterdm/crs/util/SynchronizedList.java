package ru.masterdm.crs.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

/**
 * Synchronizes two list.
 * Keep both lists in synchronized state.
 * @param <T1> type contained at first list
 * @param <T2> type contained at second list
 * @author Pavel Masalov
 */
public class SynchronizedList<T1, T2> {

    private Nut<T1, T2> listT1;
    private Nut<T2, T1> listT2;

    /**
     * One half of synchronization structure.
     * @param <T> own type
     * @param <S> type at synchronized list
     */
    protected final class Nut<T, S> implements List<T> {

        private List<T> innerList;
        private List<S> syncList;

        private Function<T, S> createSbyT;
        private Function<T, S> mapTtoS;
        private Function<S, T> mapStoT;

        /**
         * Create element with its own lists.
         * One of the map function should be defined at least.
         * @param createSbyT creator function of synchronized by own types
         * @param mapTtoS function to map own element to synchronized
         * @param mapStoT function to map synchronized element to own
         */
        private Nut(Function<T, S> createSbyT, Function<T, S> mapTtoS, Function<S, T> mapStoT) {
            this.createSbyT = createSbyT;
            this.mapTtoS = mapTtoS;
            this.mapStoT = mapStoT;
            innerList = new ArrayList<>();
        }

        /**
         * Create element with its external lists.
         * One of the map function should be defined at least.
         * @param innerList own list
         * @param syncList synchronised list
         * @param createSbyT creator function of synchronized by own types
         * @param mapTtoS function to map own element to synchronized
         * @param mapStoT function to map synchronized element to own
         */
        private Nut(List<T> innerList, List<S> syncList, Function<T, S> createSbyT, Function<T, S> mapTtoS, Function<S, T> mapStoT) {
            this.createSbyT = createSbyT;
            this.mapTtoS = mapTtoS;
            this.mapStoT = mapStoT;
            this.innerList = innerList;
            this.syncList = syncList;
        }

        @Override
        public T remove(int index) {
            T t = innerList.remove(index);
            if (t != null) {
                removeSyncByT(t);
            }
            return t;
        }

        /**
         * Utility to remove sync element by own.
         * @param t own element
         */
        private void removeSyncByT(T t) {
            if (mapTtoS != null) {
                S s = mapTtoS.apply(t);
                syncList.remove(s);
            } else if (mapStoT != null) {
                for (S s : syncList) { // back map and find by iterate
                    T lt = mapStoT.apply(s);
                    if (t.equals(lt)) {
                        syncList.remove(s);
                        break;
                    }
                }
            } else {
                throw new RuntimeException("Map functions is null");
            }
        }

        @Override
        public boolean add(T t) {
            addSyncByT(t);
            return innerList.add(t);
        }

        /**
         * Utility to add sync object ty main object.
         * @param t main object
         */
        private void addSyncByT(T t) {
            if (createSbyT != null) {
                S s = createSbyT.apply(t);
                if (s != null)
                    syncList.add(s);
            } else {
                throw new RuntimeException("Create sync by T function is null");
            }
        }

        @Override
        public boolean remove(Object o) {
            boolean r = innerList.remove(o);
            if (r) {
                removeSyncByT((T) o);
            }
            return r;
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            boolean r = innerList.addAll(c);
            if (r) {
                addSyncAllByT(c);
            }
            return r;
        }

        /**
         * Utility to add all sync elements by own elements.
         * @param c own element collection
         */
        private void addSyncAllByT(Collection<? extends T> c) {
            for (T t : c) {
                addSyncByT(t);
            }
        }

        @Override
        public boolean addAll(int index, Collection<? extends T> c) {
            boolean r = innerList.addAll(index, c);
            if (r) {
                addSyncAllByT(c);
            }
            return r;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean r = innerList.removeAll(c);
            if (r) {
                for (Object t : c) {
                    removeSyncByT((T) t);
                }
            }
            return r;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            boolean r = innerList.retainAll(c);
            if (r) {
                Collection<S> sc = new ArrayList<>(c.size());
                for (Object t : c) {
                    S s = createSbyT.apply((T) t);
                    if (s != null)
                        sc.add(s);
                }
                if (!sc.isEmpty())
                    syncList.retainAll(sc);
            }
            return r;
        }

        @Override
        public void clear() {
            innerList.clear();
            syncList.clear();
        }

        @Override
        public T set(int index, T element) {
            T t = innerList.set(index, element);
            if (t != null) {
                removeSyncByT(t);
            }
            addSyncByT(t);
            return t;
        }

        @Override
        public void add(int index, T element) {
            innerList.add(index, element);
            S s = createSbyT.apply(element);
            if (s != null)
                syncList.add(s);
        }

        @Override
        public int size() {
            return innerList.size();
        }

        @Override
        public boolean isEmpty() {
            return innerList.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return innerList.contains(o);
        }

        @Override
        public Iterator<T> iterator() {
            return innerList.iterator();
        }

        @Override
        public Object[] toArray() {
            return innerList.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return innerList.toArray(a);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return innerList.containsAll(c);
        }

        @Override
        public T get(int index) {
            return innerList.get(index);
        }

        @Override
        public int indexOf(Object o) {
            return innerList.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return innerList.lastIndexOf(o);
        }

        @Override
        public ListIterator<T> listIterator() {
            return innerList.listIterator();
        }

        @Override
        public ListIterator<T> listIterator(int index) {
            return innerList.listIterator(index);
        }

        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            return innerList.subList(fromIndex, toIndex);
        }

    }

    /**
     * Construct synchronizer with own tow lists.
     * At least one of map function should be defined
     * @param createT2ByT1 creator function of second type by first type
     * @param createT1ByT2 creator function of first type by second type
     * @param mapT1toT2 function to map first element to second
     * @param mapT2toT1 function to map second element to first
     */
    public SynchronizedList(Function<T1, T2> createT2ByT1, Function<T2, T1> createT1ByT2, Function<T1, T2> mapT1toT2, Function<T2, T1> mapT2toT1) {
        listT1 = new Nut<T1, T2>(createT2ByT1, mapT1toT2, mapT2toT1);
        listT2 = new Nut<T2, T1>(createT1ByT2, mapT2toT1, mapT1toT2);

        listT1.syncList = listT2.innerList;
        listT2.syncList = listT1.innerList;
    }

    /**
     * Construct synchronizer with own tow lists.
     * At least one of map function should be defined
     * @param innerListT1 first list
     * @param innerListT2 second list
     * @param createT2ByT1 creator function of second type by first type
     * @param createT1ByT2 creator function of first type by second type
     * @param mapT1toT2 function to map first element to second
     * @param mapT2toT1 function to map second element to first
     */
    public SynchronizedList(List<T1> innerListT1, List<T2> innerListT2, Function<T1, T2> createT2ByT1, Function<T2, T1> createT1ByT2,
                            Function<T1, T2> mapT1toT2, Function<T2, T1> mapT2toT1) {
        listT1 = new Nut<T1, T2>(innerListT1, innerListT2, createT2ByT1, mapT1toT2, mapT2toT1);
        listT2 = new Nut<T2, T1>(innerListT2, innerListT1, createT1ByT2, mapT2toT1, mapT1toT2);
    }

    /**
     * Returns list of first elements.
     * @return list of first elements
     */
    public List<T1> getListT1() {
        return listT1;
    }

    /**
     * Returns list of second elements.
     * @return list of second elements
     */
    public List<T2> getListT2() {
        return listT2;
    }

    /**
     * Return original list object.
     * May be used to change original list without synchronization
     * @return original list object
     */
    public List<T2> getListT2InnerList() {
        return listT2.innerList;
    }

    /**
     * Return original list object.
     * May be used to change original list without synchronization
     * @return original list object
     */
    public List<T1> getListT1InnerList() {
        return listT1.innerList;
    }
}
