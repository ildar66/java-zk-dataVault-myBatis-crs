package ru.masterdm.crs.test.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.function.Function;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import ru.masterdm.crs.util.SynchronizedList;

/**
 * Synchronized list tests.
 * @author Pavel Masalov
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SynchronizedListTest {

    /**
     * Container.
     */
    class Cont {

        private String s;

        /**
         * Create simple string container.
         * @param s string
         */
        Cont(String s) {
            this.s = s;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Cont)) return false;

            Cont cont = (Cont) o;

            return s != null ? s.equals(cont.s) : cont.s == null;
        }

        @Override
        public int hashCode() {
            return s != null ? s.hashCode() : 0;
        }

        @Override
        public String toString() {
            return super.toString() + "s='" + s;
        }
    }

    /**
     * Synchronized list factory.
     * @return synch list
     */
    private SynchronizedList<Cont, String> createSync() {
        return new SynchronizedList((Function<Cont, String>) (Cont c) -> c.s,
                                    (Function<String, Cont>) (String s) -> new Cont(s),
                                    (Function<Cont, String>) (Cont c) -> c.s, null);
    }

    /**
     * Test single add.
     */
    @Test
    public void test01() {
        SynchronizedList<Cont, String> s = createSync();
        Cont s1 = new Cont("S1");
        Cont s2 = new Cont("S2");
        s.getListT1().add(s1);
        assertThat(s.getListT1()).containsExactly(s1);
        assertThat(s.getListT2()).containsExactly(s1.s);
        s.getListT2().add(s2.s);
        assertThat(s.getListT1()).containsExactly(s1, s2);
        assertThat(s.getListT2()).containsExactly(s1.s, s2.s);

        s.getListT1().remove(s1);
        assertThat(s.getListT1()).containsExactly(s2);
        assertThat(s.getListT2()).containsExactly(s2.s);
        s.getListT1().remove(s2);
        assertThat(s.getListT1()).isEmpty();
        assertThat(s.getListT2()).isEmpty();
    }

    /**
     * Test all add.
     */
    @Test
    public void test02() {
        SynchronizedList<Cont, String> s = createSync();
        Cont s1 = new Cont("S1");
        Cont s2 = new Cont("S2");
        Cont s3 = new Cont("S2");
        s.getListT1().addAll(Arrays.asList(s1, s2, s3));
        assertThat(s.getListT1()).containsExactly(s1, s2, s3);
        assertThat(s.getListT2()).containsExactly(s1.s, s2.s, s3.s);

        s.getListT2().remove(s2.s);
        assertThat(s.getListT1()).containsExactly(s1, s3);
        assertThat(s.getListT2()).containsExactly(s1.s, s3.s);
    }
}
