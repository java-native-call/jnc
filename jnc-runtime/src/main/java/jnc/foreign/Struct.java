package jnc.foreign;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jnc.foreign.annotation.Pack;
import jnc.foreign.internal.AnnotationUtil;

@SuppressWarnings({"FinalMethod", "PublicInnerClass", "ProtectedInnerClass", "PublicConstructorInNonPublicClass", "WeakerAccess", "FinalClass"})
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
    private static int getPack(Class<?> type) {
        Pack pack = AnnotationUtil.getClassAnnotation(type, Pack.class);
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

    private int size;
    private int alignment = 1;
    private jnc.foreign.Pointer memory;
    private Struct enclosing;
    private int offset;
    private State state = State.INITIAL;
    private final int pack = getPack(getClass());

    private int addField0(int offset, int size, int alignment) {
        checkState(State.FIELDS_ADDING, State.FIELDS_ADDING);
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
        advance(State.FIELDS_FINISHED);
        return align(size, alignment);
    }

    final int sizeInternal() {
        return size;
    }

    void setSize(int size) {
        this.size = size;
    }

    public final int alignment() {
        advance(State.FIELDS_FINISHED);
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

    @Nullable
    public final Struct getEnclosing() {
        advance(State.FIELDS_FINISHED);
        return enclosing;
    }

    final void setEnclosing(Struct enclosing, int offset) {
        checkState(State.FIELDS_FINISHED, State.ENCLOSING_ASSIGNED);
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

    protected final <T extends Struct> T inner(@Nonnull T struct) {
        struct.setEnclosing(this, addField(struct.size(), struct.alignment()));
        return struct;
    }

    @Nonnull
    protected final int8_t[] array(@Nonnull int8_t[] array) {
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

    @Nonnull
    protected final int16_t[] array(@Nonnull int16_t[] array) {
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

    @Nonnull
    protected final int32_t[] array(@Nonnull int32_t[] array) {
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

    @Nonnull
    protected final int64_t[] array(@Nonnull int64_t[] array) {
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

    @Nonnull
    protected final uint8_t[] array(@Nonnull uint8_t[] array) {
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

    @Nonnull
    protected final uint16_t[] array(@Nonnull uint16_t[] array) {
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

    @Nonnull
    protected final uint32_t[] array(@Nonnull uint32_t[] array) {
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

    @Nonnull
    protected final uint64_t[] array(@Nonnull uint64_t[] array) {
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

    @Nonnull
    protected final bool[] array(@Nonnull bool[] array) {
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

    @Nonnull
    protected final Address[] array(@Nonnull Address[] array) {
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

    @Nonnull
    protected final Pointer[] array(@Nonnull Pointer[] array) {
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

    @Nonnull
    protected final size_t[] array(@Nonnull size_t[] array) {
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

    @Nonnull
    protected final uintptr_t[] array(@Nonnull uintptr_t[] array) {
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

    @Nonnull
    protected final intptr_t[] array(@Nonnull intptr_t[] array) {
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

    @Nonnull
    protected final clong[] array(@Nonnull clong[] array) {
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

    @Nonnull
    protected final Float32[] array(@Nonnull Float32[] array) {
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

    @Nonnull
    protected final Float64[] array(@Nonnull Float64[] array) {
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

    @Nonnull
    protected final BYTE[] array(@Nonnull BYTE[] array) {
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

    @Nonnull
    protected final WBOOL[] array(@Nonnull WBOOL[] array) {
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

    @Nonnull
    protected final WORD[] array(@Nonnull WORD[] array) {
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

    @Nonnull
    protected final DWORD[] array(@Nonnull DWORD[] array) {
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

    @Nonnull
    protected final DWORDLONG[] array(@Nonnull DWORDLONG[] array) {
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

    @Nonnull
    public final Padding padding(int size) {
        return inner(new Padding(size));
    }

    @Nonnull
    public final Padding padding(int size, int alignment) {
        return inner(new Padding(size, alignment));
    }

    @Nonnull
    public final <E extends Enum<E>> EnumField<E> enumField(Class<E> klass) {
        return new EnumField<>(klass);
    }

    private enum State {

        INITIAL,
        FIELDS_ADDING,
        FIELDS_FINISHED,
        ENCLOSING_ASSIGNED,
        MEMORY_ALLOCATED;

        void throwException(Struct struct, State advance) {
            throw new IllegalStateException("status of struct: " + this + ", can't advance to " + advance);
        }

    }

    private class BaseField {

        private final int offset;
        private final Type type;

        BaseField(Type type) {
            this.offset = addField(type.size(), type.alignment());
            this.type = type;
        }

        BaseField(NativeType nativeType) {
            this(getForeign().findType(nativeType));
        }

        final int getOffset() {
            return offset;
        }

        final Type getType() {
            return type;
        }
    }

    private abstract class NumberField extends BaseField {

        NumberField(Type type) {
            super(type);
        }

        NumberField(NativeType nativeType) {
            super(nativeType);
        }

        final void putBoolean(boolean value) {
            getMemory().putBoolean(offset, super.getType(), value);
        }

        final void putInt(int value) {
            getMemory().putInt(super.getOffset(), super.getType(), value);
        }

        final void putLong(long value) {
            getMemory().putLong(super.getOffset(), super.getType(), value);
        }

        final void putFloat(float value) {
            getMemory().putFloat(super.getOffset(), super.getType(), value);
        }

        final void putDouble(double value) {
            getMemory().putDouble(super.getOffset(), super.getType(), value);
        }

        final boolean booleanValue() {
            return getMemory().getBoolean(super.getOffset(), super.getType());
        }

        public final short shortValue() {
            return getMemory().getShort(super.getOffset(), super.getType());
        }

        public final byte byteValue() {
            return getMemory().getByte(super.getOffset(), super.getType());
        }

        public final int intValue() {
            return getMemory().getInt(super.getOffset(), super.getType());
        }

        public final long longValue() {
            return getMemory().getLong(super.getOffset(), super.getType());
        }

        public final float floatValue() {
            return getMemory().getFloat(super.getOffset(), super.getType());
        }

        public final double doubleValue() {
            return getMemory().getDouble(super.getOffset(), super.getType());
        }

        final jnc.foreign.Pointer getPointer() {
            return getMemory().getPointer(super.getOffset());
        }

        final void setPointer(jnc.foreign.Pointer value) {
            getMemory().putPointer(super.getOffset(), value);
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

    protected final class EnumField<E extends Enum<E>> {

        private final TypeHandler<E> typeHandler;
        private final BaseField field;

        EnumField(Class<E> type) {
            typeHandler = getForeign().findTypeHandler(type);
            field = new BaseField(typeHandler.nativeType());
        }

        public final E get() {
            return typeHandler.get(getMemory(), field.getOffset());
        }

        public final void set(E e) {
            typeHandler.set(getMemory(), field.getOffset(), e);
        }
    }

}
