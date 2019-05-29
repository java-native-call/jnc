package jnc.foreign.internal;

import jnc.foreign.NativeType;
import jnc.foreign.enums.TypeAlias;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeFactoryTest {

    private static final Logger log = LoggerFactory.getLogger(TypeFactoryTest.class);

    @Test
    public void testValues() {
        log.info("values");
        int width = BuiltinType.POINTER.size() * 2 + 2;
        for (NativeType nativeType : NativeType.values()) {
            InternalType value = DefaultForeign.INSTANCE.getTypeFactory().findByNativeType(nativeType);
            String name = nativeType.name();
            String address = String.format("%#0" + width + "x", value.address());
            int type = value.type();
            int size = value.size();
            int alignment = value.alignment();
            char signed = value.isSigned() ? 's' : 'u';
            log.info("{},addr={},type={},size={},align={},{}", name, address, type, size, alignment, signed);
        }
    }

    /**
     * Test of findByAlias method, of class BuiltinType.
     */
    @Test
    public void testFindAlias() {
        log.info("findByAlias");
        assertEquals(BuiltinType.UINT8.address(), DefaultForeign.INSTANCE.getTypeFactory().findByAlias(TypeAlias.uint8_t).address());
        assertEquals(BuiltinType.SINT8.address(), DefaultForeign.INSTANCE.getTypeFactory().findByAlias(TypeAlias.int8_t).address());
        assertEquals(BuiltinType.UINT16.address(), DefaultForeign.INSTANCE.getTypeFactory().findByAlias(TypeAlias.uint16_t).address());
        assertEquals(BuiltinType.SINT16.address(), DefaultForeign.INSTANCE.getTypeFactory().findByAlias(TypeAlias.int16_t).address());
        assertEquals(BuiltinType.UINT32.address(), DefaultForeign.INSTANCE.getTypeFactory().findByAlias(TypeAlias.uint32_t).address());
        assertEquals(BuiltinType.SINT32.address(), DefaultForeign.INSTANCE.getTypeFactory().findByAlias(TypeAlias.int32_t).address());
        assertEquals(BuiltinType.SINT64.address(), DefaultForeign.INSTANCE.getTypeFactory().findByAlias(TypeAlias.int64_t).address());
        assertEquals(BuiltinType.UINT64.address(), DefaultForeign.INSTANCE.getTypeFactory().findByAlias(TypeAlias.uint64_t).address());
        Alias intptr_t = DefaultForeign.INSTANCE.getTypeFactory().findByAlias(TypeAlias.intptr_t);
        Alias uintptr_t = DefaultForeign.INSTANCE.getTypeFactory().findByAlias(TypeAlias.uintptr_t);
        assertEquals(TypeAlias.uintptr_t, uintptr_t.getTypeAlias());
        assertTrue(uintptr_t.toString().contains("uintptr_t"));
        assertEquals(BuiltinType.POINTER.size(), intptr_t.size());
        assertEquals(BuiltinType.POINTER.size(), uintptr_t.size());
        assertTrue(intptr_t.isSigned());
        assertFalse(uintptr_t.isSigned());
    }

}
