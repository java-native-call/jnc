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
            if (value > 0) {
                if ((value & (value - 1)) != 0 || value > MAX_ALIGN) {
                    throw new IllegalArgumentException("expected pack parameter to be '1', '2', '4', '8', or '16'");
                }
                return value;
            }
            if (value != 0) {
                throw new IllegalArgumentException("Illegal pack value " + value);
            }
        }
        return MAX_ALIGN;
    }

    private final int pack;
    private int size;
    private int alignment = 1;
    private jnc.foreign.Pointer memory;
    private Struct enclosing;
    private int offset;
    private State state = State.INITIAL;

    public Struct() {
        this.pack = getPack(getClass());
    }

    private int addField0(int offset, int size, int alignment) {
        checkState(State.FIELD_ADDING, State.FIELD_ADDING);
        this.size = Math.max(this.size, offset + checkSize(size));
        this.alignment = Math.max(this.alignment, Math.min(alignment, pack));
        return offset;
    }

    int nextOffset(int alignment) {
        return align(this.size, Math.min(pack, alignment));
    }

    /**
     *
     * @return offset of the field
     */
    final int addField(int size, int alignment) {
        return addField0(nextOffset(alignment), size, alignment);
    }

    private void advance(State to) {
        if (state.ordinal() < to.ordinal()) {
            state = to;
        }
    }

    private void checkState(State check, State advance) {
        if (state.ordinal() <= check.ordinal()) {
            advance(advance);
            return;
        }
        state.throwException(this, advance);
    }

    public final int size() {
        advance(State.FIELD_FINISH);
        return align(size, alignment);
    }

    final int sizeInternal() {
        return size;
    }

    void setSize(int size) {
        this.size = size;
    }

    public final int alignment() {
        advance(State.FIELD_FINISH);
        return alignment;
    }

    public final Foreign getForeign() {
        return ForeignProviders.getDefault();
    }

    public final jnc.foreign.Pointer getMemory() {
        advance(State.MEMORY_ALLOCATED);
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
        advance(State.FIELD_FINISH);
        return enclosing;
    }

    final void setEnclosing(Struct enclosing, int offset) {
        checkState(State.FIELD_FINISH, State.OUTTER_ASSIGNED);
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

    void arrayBegin() {
    }

    void arrayEnd() {
    }

    protected final <T extends Struct> T inner(T struct) {
        struct.setEnclosing(this, addField(struct.size(), struct.alignment()));
        return struct;
    }

    protected final int8_t[] array(int8_t[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new int8_t();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final int16_t[] array(int16_t[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new int16_t();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final int32_t[] array(int32_t[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new int32_t();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final int64_t[] array(int64_t[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new int64_t();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final uint8_t[] array(uint8_t[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new uint8_t();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final uint16_t[] array(uint16_t[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new uint16_t();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final uint32_t[] array(uint32_t[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new uint32_t();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final uint64_t[] array(uint64_t[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new uint64_t();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final bool[] array(bool[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new bool();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final Address[] array(Address[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new Address();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final Pointer[] array(Pointer[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new Pointer();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final size_t[] array(size_t[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new size_t();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final uintptr_t[] array(uintptr_t[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new uintptr_t();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final intptr_t[] array(intptr_t[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new intptr_t();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final clong[] array(clong[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new clong();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final Float32[] array(Float32[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new Float32();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final Float64[] array(Float64[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new Float64();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final BYTE[] array(BYTE[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new BYTE();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final WBOOL[] array(WBOOL[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new WBOOL();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final WORD[] array(WORD[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new WORD();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final DWORD[] array(DWORD[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new DWORD();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    protected final DWORDLONG[] array(DWORDLONG[] array) {
        arrayBegin();
        try {
            for (int i = 0, len = array.length; i < len; ++i) {
                array[i] = new DWORDLONG();
            }
        } finally {
            arrayEnd();
        }
        return array;
    }

    public final Padding padding(int size) {
        return inner(new Padding(size));
    }

    public final Padding padding(int size, int alignment) {
        return inner(new Padding(size, alignment));
    }

    private static enum State {

        INITIAL,
        FIELD_ADDING,
        FIELD_FINISH,
        OUTTER_ASSIGNED,
        MEMORY_ALLOCATED;

        void throwException(Struct struct, State advance) {
            throw new IllegalStateException("status of struct: " + this + ", can't advance to " + advance);
        }

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

        public final boolean get() {
            return booleanValue();
        }

        public final void set(boolean value) {
            putBoolean(value);
        }

    }

    protected class int8_t extends NumberField {

        public int8_t() {
            super(NativeType.SINT8);
        }

        public final byte get() {
            return byteValue();
        }

        public final void set(byte value) {
            putInt(value);
        }

    }

    protected class int16_t extends NumberField {

        public int16_t() {
            super(NativeType.SINT16);
        }

        public final short get() {
            return shortValue();
        }

        public final void set(short value) {
            putInt(value);
        }

    }

    protected class int32_t extends NumberField {

        public int32_t() {
            super(NativeType.SINT32);
        }

        public final int get() {
            return intValue();
        }

        public final void set(int value) {
            putInt(value);
        }

    }

    private class LongField extends NumberField {

        LongField(Type type) {
            super(type);
        }

        LongField(NativeType nativeType) {
            this(getForeign().findType(nativeType));
        }

        public final long get() {
            return longValue();
        }

        public final void set(long value) {
            putLong(value);
        }

    }

    protected class int64_t extends LongField {

        public int64_t() {
            super(NativeType.SINT64);
        }

    }

    protected class uint8_t extends NumberField {

        public uint8_t() {
            super(NativeType.UINT8);
        }

        public final short get() {
            return byteValue();
        }

        public final void set(short value) {
            putInt(value);
        }

    }

    protected class uint16_t extends NumberField {

        public uint16_t() {
            super(NativeType.UINT16);
        }

        public final char get() {
            return (char) intValue();
        }

        public final void set(char value) {
            putInt(value);
        }

    }

    protected class uint32_t extends LongField {

        public uint32_t() {
            super(NativeType.UINT32);
        }

    }

    protected class uint64_t extends LongField {

        public uint64_t() {
            super(NativeType.UINT64);
        }

    }

    protected class bool extends AbstraceBoolean {

        public bool() {
            super(NativeType.UINT8);
        }

    }

    protected class Address extends LongField {

        public Address() {
            super(NativeType.ADDRESS);
        }

    }

    protected class Pointer {

        private final Address address = new Address();

        public Pointer() {
        }

        public final jnc.foreign.Pointer get() {
            return address.getPointer();
        }

        public final void set(jnc.foreign.Pointer value) {
            address.setPointer(value);
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

        public final float get() {
            return floatValue();
        }

        public final void set(float value) {
            putFloat(value);
        }

    }

    protected class Float64 extends NumberField {

        public Float64() {
            super(NativeType.DOUBLE);
        }

        public final double get() {
            return doubleValue();
        }

        public final void set(double value) {
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
