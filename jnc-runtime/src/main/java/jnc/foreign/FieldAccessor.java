package jnc.foreign;

// Don't use, internal support for enum type
@Deprecated
public interface FieldAccessor<T> {

    Type type();

    T get(Pointer memory, int offset);

    void set(Pointer memory, int offset, T value);

}
