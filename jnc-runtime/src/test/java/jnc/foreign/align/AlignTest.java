package jnc.foreign.align;

import jnc.foreign.Struct;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class AlignTest {

    @Test
    public void testAlign() {
        Struct a = new jnc.foreign.align.align2.Sample();
        Struct b = new jnc.foreign.align.align4.Sample();
        assertEquals(6, a.size());
        assertEquals(2, a.alignment());
        assertEquals(8, b.size());
        assertEquals(4, b.alignment());
    }

}
