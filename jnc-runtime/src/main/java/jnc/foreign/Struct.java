package jnc.foreign;

import jnc.foreign.annotation.Pack;
import jnc.foreign.internal.AnnotationUtil;

@SuppressWarnings({"FinalMethod", "PublicInnerClass", "ProtectedInnerClass", "PublicConstructorInNonPublicClass", "FinalClass"})
public class Struct {

    private static final int MAX_ALIGN = 16;

    private static int align(int size, int alignment) {
        return size + alignment - 1 & -alignment;
    }

    private static int checkSize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Illegal size " + size);
        }
        if (size > (Integer.MAX_VALUE & -MAX_ALIGN)) {
            throw new OutOfMemoryError("size too large " + size);
        }
        return size;
    }

    // visiable for testing
    static int getPack(Class<?> type) {
        Pack pack = AnnotationUtil.getAnnotation(type, Pack.class);
        if (pack != null) {
            int value = pack.value();
            if (value <= 0) {
                throw new IllegalArgumentException("Illegal pack value " + value);
            }
            if ((value & (value - 1)) != 0 || value > MAX_ALIGN) {
                throw new IllegalArgumentException("expected pack parameter to be '1', '2', '4', '8', or '16'");
            }
            return value;
        }
        return MAX_ALIGN;
    }

    private final int pack;
    private int size;
    private int alignment = 1;
    private jnc.foreign.Pointer memory;
    private Struct enclosing;
    private int offset;
    private String finish;

    public Struct() {
        this.pack = getPack(getClass());
    }

    private int addField0(int offset, int size, int alignment) {
        checkFinish();
        this.size = Math.max(this.size, offset + checkSize(size));
        this.alignment = Math.max(this.alignment, Math.min(alignment, pack));
        return offset;
    }

    /**
     *
     * @return offset of the field
     */
    int addField(int size, int alignment) {
        return addField0(align(this.size, Math.min(pack, alignment)), size, alignment);
    }

    private void finish(String reason) {
        finish = reason;
    }

    private void checkFinish() {
        if (finish != null) {
            throw new IllegalStateException("Add field after call method '" + finish + "' on '" + this + "', adding field is only allowed in the constructor and before calling size()");
        }
    }

    public final int size() {
        finish("size()");
        return align(size, alignment);
    }

    public final int alignment() {
        finish("alignment()");
        return alignment;
    }

    public final Foreign getForeign() {
        return ForeignProviders.getDefault();
    }

    public final jnc.foreign.Pointer getMemory() {
        finish("getMemory()");
        jnc.foreign.Pointer m = memory;
        if (m == null) {
            Struct enclose = enclosing;
            if (enclose != null) {
                m = enclose.getMemory().slice(offset, size());
            } else {
                m = getForeign().getMemoryManager().allocateWithAlign(size(), alignment());
            }
            memory = m;
        }
        return m;
    }

    public final Struct getEnclosing() {
        finish("getEnclosing()");
        return enclosing;
    }

    final void setEnclosing(Struct enclosing, int offset) {
        finish("struct.inner(this)");
        this.enclosing = enclosing;
        this.offset = offset;
    }

    @Override
    public String toString() {
        String name = getClass().getSimpleName();
        if (memory != null) {
            return name + "[" + memory + "]";
        }
        return name;
    }

    protected final <T extends Struct> T inner(T struct) {
        struct.setEnclosing(this, addField(struct.size(), struct.alignment()));
        return struct;
    }

    protected final int8_t[] array(int8_t[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new int8_t();
        }
        return array;
    }

    protected final int16_t[] array(int16_t[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new int16_t();
        }
        return array;
    }

    protected final int32_t[] array(int32_t[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new int32_t();
        }
        return array;
    }

    protected final int64_t[] array(int64_t[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new int64_t();
        }
        return array;
    }

    protected final uint8_t[] array(uint8_t[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new uint8_t();
        }
        return array;
    }

    protected final uint16_t[] array(uint16_t[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new uint16_t();
        }
        return array;
    }

    protected final uint32_t[] array(uint32_t[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new uint32_t();
        }
        return array;
    }

    protected final uint64_t[] array(uint64_t[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new uint64_t();
        }
        return array;
    }

    protected final bool[] array(bool[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new bool();
        }
        return array;
    }

    protected final Pointer[] array(Pointer[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new Pointer();
        }
        return array;
    }

    protected final Address[] array(Address[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new Address();
        }
        return array;
    }

    protected final size_t[] array(size_t[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new size_t();
        }
        return array;
    }

    protected final uintptr_t[] array(uintptr_t[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new uintptr_t();
        }
        return array;
    }

    protected final intptr_t[] array(intptr_t[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new intptr_t();
        }
        return array;
    }

    protected final clong[] array(clong[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new clong();
        }
        return array;
    }

    protected final Float32[] array(Float32[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new Float32();
        }
        return array;
    }

    protected final Float64[] array(Float64[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new Float64();
        }
        return array;
    }

    protected final BYTE[] array(BYTE[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new BYTE();
        }
        return array;
    }

    protected final WBOOL[] array(WBOOL[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new WBOOL();
        }
        return array;
    }

    protected final WORD[] array(WORD[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new WORD();
        }
        return array;
    }

    protected final DWORD[] array(DWORD[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new DWORD();
        }
        return array;
    }

    protected final DWORDLONG[] array(DWORDLONG[] array) {
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = new DWORDLONG();
        }
        return array;
    }

    private abstract class NumberField {

        private final int offset;
        private final Type type;

        NumberField(Type type) {
            this.offset = addField(type.size(), type.alignment());
            this.type = type;
        }

        NumberField(NativeType nativeType) {
            this(getForeign().findType(nativeType));
        }

        final void putBoolean(boolean value) {
            getMemory().putBoolean(offset, type, value);
        }

        final void putInt(int value) {
            getMemory().putInt(offset, type, value);
        }

        final void putLong(long value) {
            getMemory().putLong(offset, type, value);
        }

        final void putFloat(float value) {
            getMemory().putFloat(offset, type, value);
        }

        final void putDouble(double value) {
            getMemory().putDouble(offset, type, value);
        }

        final boolean booleanValue() {
            return getMemory().getBoolean(offset, type);
        }

        public final short shortValue() {
            return getMemory().getShort(offset, type);
        }

        public final byte byteValue() {
            return getMemory().getByte(offset, type);
        }

        public final int intValue() {
            return getMemory().getInt(offset, type);
        }

        public final long longValue() {
            return getMemory().getLong(offset, type);
        }

        public final float floatValue() {
            return getMemory().getFloat(offset, type);
        }

        public final double doubleValue() {
            return getMemory().getDouble(offset, type);
        }

        final jnc.foreign.Pointer getPointer() {
            return getMemory().getPointer(offset);
        }

        final void setPointer(jnc.foreign.Pointer value) {
            getMemory().putPointer(offset, value);
        }

    }

    private abstract class AbstraceBoolean extends NumberField {

        AbstraceBoolean(NativeType type) {
            super(type);
        }

        public boolean get() {
            return booleanValue();
        }

        public void set(boolean value) {
            putBoolean(value);
        }

    }

    protected class int8_t extends NumberField {

        public int8_t() {
            super(NativeType.SINT8);
        }

        public byte get() {
            return byteValue();
        }

        public void set(byte value) {
            putInt(value);
        }

    }

    protected class int16_t extends NumberField {

        public int16_t() {
            super(NativeType.SINT16);
        }

        public short get() {
            return shortValue();
        }

        public void set(short value) {
            putInt(value);
        }

    }

    protected class int32_t extends NumberField {

        public int32_t() {
            super(NativeType.SINT32);
        }

        public int get() {
            return intValue();
        }

        public void set(int value) {
            putInt(value);
        }

    }

    protected class int64_t extends NumberField {

        public int64_t() {
            super(NativeType.SINT64);
        }

        public long get() {
            return longValue();
        }

        public void set(long value) {
            putLong(value);
        }

    }

    protected class uint8_t extends NumberField {

        public uint8_t() {
            super(NativeType.UINT8);
        }

        public short get() {
            return byteValue();
        }

        public void set(short value) {
            putInt(value);
        }

    }

    protected class uint16_t extends NumberField {

        public uint16_t() {
            super(NativeType.UINT16);
        }

        public char get() {
            return (char) intValue();
        }

        public void set(char value) {
            putInt(value);
        }

    }

    protected class uint32_t extends NumberField {

        public uint32_t() {
            super(NativeType.UINT32);
        }

        public long get() {
            return longValue();
        }

        public void set(long value) {
            putLong(value);
        }

    }

    protected class uint64_t extends NumberField {

        public uint64_t() {
            super(NativeType.UINT64);
        }

        public long get() {
            return longValue();
        }

        public void set(long value) {
            putLong(value);
        }

    }

    protected class bool extends AbstraceBoolean {

        protected bool() {
            super(NativeType.UINT8);
        }

    }

    protected class Pointer {

        private final Address address = new Address();

        public jnc.foreign.Pointer get() {
            return address.getPointer();
        }

        public void set(jnc.foreign.Pointer value) {
            address.setPointer(value);
        }

    }

    private class LongField extends NumberField {

        LongField(Type type) {
            super(type);
        }

        LongField(NativeType nativeType) {
            this(getForeign().findType(nativeType));
        }

        public long get() {
            return longValue();
        }

        public void set(long value) {
            putLong(value);
        }

    }

    protected class Address extends LongField {

        public Address() {
            super(NativeType.ADDRESS);
        }

    }

    protected class size_t extends LongField {

        public size_t() {
            super(getForeign().findType("size_t"));
        }

    }

    protected class uintptr_t extends LongField {

        public uintptr_t() {
            super(getForeign().findType("uintptr_t"));
        }

    }

    protected class intptr_t extends LongField {

        public intptr_t() {
            super(getForeign().findType("intptr_t"));
        }

    }

    protected class clong extends LongField {

        public clong() {
            super(getForeign().findType("long"));
        }

    }

    protected class Float32 extends NumberField {

        public Float32() {
            super(NativeType.FLOAT);
        }

        public float get() {
            return floatValue();
        }

        public void set(float value) {
            putFloat(value);
        }

    }

    protected class Float64 extends NumberField {

        public Float64() {
            super(NativeType.DOUBLE);
        }

        public double get() {
            return doubleValue();
        }

        public void set(double value) {
            putDouble(value);
        }

    }

    /**
     * BYTE in windows, unsigned char
     */
    protected class BYTE extends uint8_t {

        public BYTE() {
        }

    }

    protected class WBOOL extends AbstraceBoolean {

        public WBOOL() {
            super(NativeType.SINT32);
        }

    }

    protected class WORD extends uint16_t {

        public WORD() {
        }

    }

    protected class DWORD extends uint32_t {

        public DWORD() {
        }

    }

    protected class DWORDLONG extends uint64_t {

        public DWORDLONG() {
        }

    }

}
