package jnc.foreign;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 * A utility class, can be used as a parameter or inner struct field
 */
@SuppressWarnings("unused")
public final class StructArray<T extends Struct> extends Struct implements Iterable<T> {

    private final List<T> list;

    public StructArray(Supplier<T> constructor, int length) {
        Objects.requireNonNull(constructor, "constructor");
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        List<T> list = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            list.add(inner(constructor.get()));
        }
        this.list = list;
    }

    public int length() {
        return list.size();
    }

    public T get(int index) {
        return list.get(index);
    }

    @Nonnull
    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        list.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return list.spliterator();
    }

    public Stream<T> stream() {
        return list.stream();
    }

}
