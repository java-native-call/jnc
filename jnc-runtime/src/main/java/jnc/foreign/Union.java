package jnc.foreign;

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
    void arrayBegin() {
        lastSize = sizeInternal();
        setSize(0);
    }

    @Override
    void arrayEnd() {
        setSize(Math.max(lastSize, sizeInternal()));
        lastSize = -1;
    }

}
