package jnc.foreign;

public interface FieldAccessor<T> {

    T get(Pointer memory, int offset);

    void set(Pointer memory, int offset, T value);

}
