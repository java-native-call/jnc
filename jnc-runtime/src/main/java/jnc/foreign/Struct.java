package jnc.foreign;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jnc.foreign.annotation.Pack;
import jnc.foreign.enums.TypeAlias;
import jnc.foreign.exception.UnmappableNativeValueException;

@SuppressWarnings({"PublicInnerClass", "ProtectedInnerClass", "PublicConstructorInNonPublicClass", "WeakerAccess", "unused"})
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

    // visible for testing
    private static int getPack(Class<?> type) {
        Pack pack = type.getAnnotation(Pack.class);
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
    private Enclosing enclosing;
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
        throw state.toException(advance);
    }

    public final int size() {
        advance(State.FIELDS_FINISHED);
        return align(size, alignment);
    }

    public final int alignment() {
        advance(State.FIELDS_FINISHED);
        return alignment;
    }

    public final Foreign getForeign() {
        return ForeignProvider.getDefault().getForeign();
    }

    public final jnc.foreign.Pointer getMemory() {
        advance(State.MEMORY_ALLOCATED);
        jnc.foreign.Pointer m = memory;
        if (m == null) {
            Enclosing enclose = enclosing;
            if (enclose != null) {
                m = enclose.getMemory(this.size());
            } else {
                m = getForeign().getMemoryManager().allocateWithAlign(size(), alignment());
            }
            memory = m;
        }
        return m;
    }

    @Nullable
    public final Enclosing getEnclosing() {
        advance(State.FIELDS_FINISHED);
        return enclosing;
    }

    final void setEnclosing(Struct enclosing, int offset) {
        checkState(State.FIELDS_FINISHED, State.ENCLOSING_ASSIGNED);
        this.enclosing = new Enclosing(enclosing, offset);
    }

    @Override
    public String toString() {
        String name = getClass().getSimpleName();
        if (memory != null) {
            return name + "[" + memory + "]";
        }
        return name;
    }

    @Nonnull
    protected final <T extends Struct> T inner(@Nonnull T struct) {
        struct.setEnclosing(this, addField(struct.size(), struct.alignment()));
        return struct;
    }

    private <T> T[] memberArray(T[] array, Function<Struct, T> constructor) {
        Objects.requireNonNull(array, "array");
        Struct struct = new Struct();
        for (int i = 0, len = array.length; i < len; ++i) {
            array[i] = constructor.apply(struct);
        }
        this.inner(struct);
        return array;
    }

    @Nonnull
    protected final int8_t[] array(@Nonnull int8_t[] array) {
        return memberArray(array, struct -> struct.new int8_t());
    }

    @Nonnull
    protected final int16_t[] array(@Nonnull int16_t[] array) {
        return memberArray(array, struct -> struct.new int16_t());
    }

    @Nonnull
    protected final int32_t[] array(@Nonnull int32_t[] array) {
        return memberArray(array, struct -> struct.new int32_t());
    }

    @Nonnull
    protected final int64_t[] array(@Nonnull int64_t[] array) {
        return memberArray(array, struct -> struct.new int64_t());
    }

    @Nonnull
    protected final uint8_t[] array(@Nonnull uint8_t[] array) {
        return memberArray(array, struct -> struct.new uint8_t());
    }

    @Nonnull
    protected final uint16_t[] array(@Nonnull uint16_t[] array) {
        return memberArray(array, struct -> struct.new uint16_t());
    }

    @Nonnull
    protected final uint32_t[] array(@Nonnull uint32_t[] array) {
        return memberArray(array, struct -> struct.new uint32_t());
    }

    @Nonnull
    protected final uint64_t[] array(@Nonnull uint64_t[] array) {
        return memberArray(array, struct -> struct.new uint64_t());
    }

    @Nonnull
    protected final bool[] array(@Nonnull bool[] array) {
        return memberArray(array, struct -> struct.new bool());
    }

    @Nonnull
    protected final Address[] array(@Nonnull Address[] array) {
        return memberArray(array, struct -> struct.new Address());
    }

    @Nonnull
    protected final Pointer[] array(@Nonnull Pointer[] array) {
        return memberArray(array, struct -> struct.new Pointer());
    }

    @Nonnull
    protected final size_t[] array(@Nonnull size_t[] array) {
        return memberArray(array, struct -> struct.new size_t());
    }

    @Nonnull
    protected final uintptr_t[] array(@Nonnull uintptr_t[] array) {
        return memberArray(array, struct -> struct.new uintptr_t());
    }

    @Nonnull
    protected final intptr_t[] array(@Nonnull intptr_t[] array) {
        return memberArray(array, struct -> struct.new intptr_t());
    }

    @Nonnull
    protected final clong[] array(@Nonnull clong[] array) {
        return memberArray(array, struct -> struct.new clong());
    }

    @Nonnull
    protected final Float32[] array(@Nonnull Float32[] array) {
        return memberArray(array, struct -> struct.new Float32());
    }

    @Nonnull
    protected final Float64[] array(@Nonnull Float64[] array) {
        return memberArray(array, struct -> struct.new Float64());
    }

    @Nonnull
    protected final BYTE[] array(@Nonnull BYTE[] array) {
        return memberArray(array, struct -> struct.new BYTE());
    }

    @Nonnull
    protected final WBOOL[] array(@Nonnull WBOOL[] array) {
        return memberArray(array, struct -> struct.new WBOOL());
    }

    @Nonnull
    protected final WORD[] array(@Nonnull WORD[] array) {
        return memberArray(array, struct -> struct.new WORD());
    }

    @Nonnull
    protected final DWORD[] array(@Nonnull DWORD[] array) {
        return memberArray(array, struct -> struct.new DWORD());
    }

    @Nonnull
    protected final DWORDLONG[] array(@Nonnull DWORDLONG[] array) {
        return memberArray(array, struct -> struct.new DWORDLONG());
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

    @Nonnull
    public final <E extends Enum<E>> EnumField<E>[] enumArray(Class<E> klass, int length) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        EnumField<E>[] array = new EnumField[length];
        return memberArray(array, arr -> arr.enumField(klass));
    }

    @Nonnull
    public final <T extends Struct> StructArray<T> structArray(Supplier<T> constructor, int length) {
        return inner(new StructArray<>(constructor, length));
    }

    public static final class Enclosing {

        private final Struct struct;
        private final int offset;

        public Enclosing(Struct struct, int offset) {
            this.struct = struct;
            this.offset = offset;
        }

        public final Struct getStruct() {
            return struct;
        }

        public final int getOffset() {
            return offset;
        }

        final jnc.foreign.Pointer getMemory(int size) {
            return struct.getMemory().slice(offset, size);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected static final class Padding extends Struct {

        Padding(int size) {
            this(size, 1);
        }

        Padding(int size, int alignment) {
            if ((alignment & alignment - 1) != 0) {
                throw new IllegalArgumentException("Illegal alignment " + alignment);
            }
            if (size < alignment) {
                throw new IllegalArgumentException("size is smaller than alignment: size=" + size + ",align=" + alignment);
            }
            addField(size, alignment);
        }

    }

    private enum State {

        INITIAL,
        FIELDS_ADDING,
        FIELDS_FINISHED,
        ENCLOSING_ASSIGNED,
        MEMORY_ALLOCATED;

        IllegalStateException toException(State advance) {
            return new IllegalStateException("status of struct: " + this + ", can't advance to " + advance);
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

    private abstract class NumberField extends Number {

        private static final long serialVersionUID = 0L;

        private final BaseField base;

        NumberField(Type type) {
            base = new BaseField(type);
        }

        NumberField(NativeType nativeType) {
            base = new BaseField(nativeType);
        }

        final void putBoolean(boolean value) {
            getMemory().putBoolean(base.getOffset(), base.getType(), value);
        }

        final void putInt(int value) {
            getMemory().putInt(base.getOffset(), base.getType(), value);
        }

        final void putLong(long value) {
            getMemory().putLong(base.getOffset(), base.getType(), value);
        }

        final void putFloat(float value) {
            getMemory().putFloat(base.getOffset(), base.getType(), value);
        }

        final void putDouble(double value) {
            getMemory().putDouble(base.getOffset(), base.getType(), value);
        }

        final boolean booleanValue() {
            return getMemory().getBoolean(base.getOffset(), base.getType());
        }

        @Override
        public final short shortValue() {
            return getMemory().getShort(base.getOffset(), base.getType());
        }

        @Override
        public final byte byteValue() {
            return getMemory().getByte(base.getOffset(), base.getType());
        }

        @Override
        public final int intValue() {
            return getMemory().getInt(base.getOffset(), base.getType());
        }

        @Override
        public final long longValue() {
            return getMemory().getLong(base.getOffset(), base.getType());
        }

        @Override
        public final float floatValue() {
            return getMemory().getFloat(base.getOffset(), base.getType());
        }

        @Override
        public final double doubleValue() {
            return getMemory().getDouble(base.getOffset(), base.getType());
        }

        @Override
        public abstract String toString();

        final jnc.foreign.Pointer getPointer() {
            return getMemory().getPointer(base.getOffset());
        }

        final void setPointer(jnc.foreign.Pointer value) {
            getMemory().putPointer(base.getOffset(), value);
        }

    }

    private abstract class AbstractBoolean extends NumberField {

        private static final long serialVersionUID = 0L;

        AbstractBoolean(NativeType type) {
            super(type);
        }

        public final boolean get() {
            return booleanValue();
        }

        public final void set(boolean value) {
            putBoolean(value);
        }

        @Override
        public String toString() {
            return Boolean.toString(get());
        }
    }

    protected class int8_t extends NumberField {

        private static final long serialVersionUID = 0L;

        public int8_t() {
            super(NativeType.SINT8);
        }

        public final byte get() {
            return byteValue();
        }

        public final void set(byte value) {
            putInt(value);
        }

        @Override
        public String toString() {
            return Integer.toString(get());
        }
    }

    protected class int16_t extends NumberField {

        private static final long serialVersionUID = 0L;

        public int16_t() {
            super(NativeType.SINT16);
        }

        public final short get() {
            return shortValue();
        }

        public final void set(short value) {
            putInt(value);
        }

        @Override
        public String toString() {
            return Integer.toString(get());
        }
    }

    protected class int32_t extends NumberField {

        private static final long serialVersionUID = 0L;

        public int32_t() {
            super(NativeType.SINT32);
        }

        public final int get() {
            return intValue();
        }

        public final void set(int value) {
            putInt(value);
        }

        @Override
        public String toString() {
            return Integer.toString(get());
        }
    }

    private abstract class LongField extends NumberField {

        private static final long serialVersionUID = 0L;

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

        private static final long serialVersionUID = 0L;

        public int64_t() {
            super(NativeType.SINT64);
        }

        @Override
        public String toString() {
            return Long.toString(get());
        }
    }

    protected class uint8_t extends NumberField {

        private static final long serialVersionUID = 0L;

        public uint8_t() {
            super(NativeType.UINT8);
        }

        public final short get() {
            return byteValue();
        }

        public final void set(short value) {
            putInt(value);
        }

        @Override
        public String toString() {
            return Integer.toString(get());
        }
    }

    protected class uint16_t extends NumberField {

        private static final long serialVersionUID = 0L;

        public uint16_t() {
            super(NativeType.UINT16);
        }

        public final char get() {
            return (char) intValue();
        }

        public final void set(char value) {
            putInt(value);
        }

        @Override
        public String toString() {
            return Integer.toString(get());
        }
    }

    protected class uint32_t extends LongField {

        private static final long serialVersionUID = 0L;

        public uint32_t() {
            super(NativeType.UINT32);
        }

        @Override
        public String toString() {
            return Long.toString(get());
        }
    }

    protected class uint64_t extends LongField {

        private static final long serialVersionUID = 0L;

        public uint64_t() {
            super(NativeType.UINT64);
        }

        @Override
        public String toString() {
            return Long.toUnsignedString(get());
        }
    }

    protected class bool extends AbstractBoolean {

        private static final long serialVersionUID = 0L;

        public bool() {
            super(NativeType.UINT8);
        }

    }

    protected class Address extends LongField {

        private static final long serialVersionUID = 0L;

        public Address() {
            super(NativeType.ADDRESS);
        }

        @Override
        public String toString() {
            return Long.toHexString(get());
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

        private static final long serialVersionUID = 0L;

        public size_t() {
            super(getForeign().findType(TypeAlias.size_t));
        }

        @Override
        public String toString() {
            return Long.toUnsignedString(get());
        }
    }

    protected class uintptr_t extends LongField {

        private static final long serialVersionUID = 0L;

        public uintptr_t() {
            super(getForeign().findType(TypeAlias.uintptr_t));
        }

        @Override
        public String toString() {
            return Long.toUnsignedString(get());
        }
    }

    protected class intptr_t extends LongField {

        private static final long serialVersionUID = 0L;

        public intptr_t() {
            super(getForeign().findType(TypeAlias.intptr_t));
        }

        @Override
        public String toString() {
            return Long.toString(get());
        }
    }

    protected class clong extends LongField {

        private static final long serialVersionUID = 0L;

        public clong() {
            super(getForeign().findType(TypeAlias.clong));
        }

        @Override
        public String toString() {
            return Long.toString(get());
        }
    }

    protected class Float32 extends NumberField {

        private static final long serialVersionUID = 0L;

        public Float32() {
            super(NativeType.FLOAT);
        }

        public final float get() {
            return floatValue();
        }

        public final void set(float value) {
            putFloat(value);
        }

        @Override
        public String toString() {
            return Float.toString(get());
        }
    }

    protected class Float64 extends NumberField {

        private static final long serialVersionUID = 0L;

        public Float64() {
            super(NativeType.DOUBLE);
        }

        public final double get() {
            return doubleValue();
        }

        public final void set(double value) {
            putDouble(value);
        }

        @Override
        public String toString() {
            return Double.toString(get());
        }
    }

    /**
     * BYTE in windows, unsigned char
     */
    protected class BYTE extends uint8_t {

        private static final long serialVersionUID = 0L;

        public BYTE() {
        }

    }

    protected class WBOOL extends AbstractBoolean {

        private static final long serialVersionUID = 0L;

        public WBOOL() {
            super(NativeType.SINT32);
        }

    }

    protected class WORD extends uint16_t {

        private static final long serialVersionUID = 0L;

        public WORD() {
        }

    }

    protected class DWORD extends uint32_t {

        private static final long serialVersionUID = 0L;

        public DWORD() {
        }

    }

    protected class DWORDLONG extends uint64_t {

        private static final long serialVersionUID = 0L;

        public DWORDLONG() {
        }

    }

    protected final class EnumField<E extends Enum<E>> {

        private final Class<E> type;
        private final FieldAccessor<E> fieldAccessor;
        private final BaseField field;

        EnumField(Class<E> type) {
            this.type = type;
            FieldAccessor<E> fa = getForeign().getEnumFieldAccessor(type);
            this.fieldAccessor = fa;
            this.field = new BaseField(fa.type());
        }

        public final E get() {
            return fieldAccessor.get(getMemory(), field.getOffset());
        }

        public final void set(E e) {
            fieldAccessor.set(getMemory(), field.getOffset(), e);
        }

        /**
         * if this enum field indicate to null, return string "null"
         */
        @Override
        public String toString() {
            try {
                return String.valueOf(get());
            } catch (UnmappableNativeValueException ex) {
                return type.getName() + "(unmappable)";
            }
        }
    }

}
