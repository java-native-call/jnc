package jnc.foreign;

import java.util.function.Consumer;

public class Union extends Struct {

    private int lastSize = -1;

    @Override
    int nextOffset(int alignment) {
        if (lastSize > -1) {
            return super.nextOffset(alignment);
        }
        return 0;
    }

    @Override
    <T> T wrapperArrayCreaion(T arr, Consumer<T> consumer) {
        lastSize = sizeInternal();
        setSize(0);
        try {
            consumer.accept(arr);
        } finally {
            setSize(Math.max(lastSize, sizeInternal()));
            lastSize = -1;
        }
        return arr;
    }

}
