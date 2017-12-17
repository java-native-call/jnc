package jnc.foreign.internal;

import jnc.foreign.Struct;

class StructArrayHandler implements ParameterHandler<Struct[]> {

    private static final StructArrayHandler INSTANCE = new StructArrayHandler();

    static StructArrayHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void handle(CallContext context, int index, Struct[] obj) {
        if (obj == null || obj.length == 0) {
            context.putLong(index, 0);
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
//            int off = 0;
//            int len = obj.length;
//            PointerArray array = PointerArray.allocate(len);
//            for (int i = 0; i < len; ++i) {
//                Struct struct = obj[i];
//                array.set(i, struct == null ? 0 : struct.getMemory().address());
//            }
//            context.onFinish(new RunnableImpl(array, off, len, obj)).putLong(index, array.address());
        }
    }

//    private static class RunnableImpl implements Runnable {
//
//        private final PointerArray array;
//        private final int off;
//        private final int len;
//        private final Struct[] obj;
//
//        RunnableImpl(PointerArray array, int off, int len, Struct[] obj) {
//            this.array = array;
//            this.off = off;
//            this.len = len;
//            this.obj = obj;
//        }
//
//        @Override
//        public void run() {
//            for (int i = 0; i < len; ++i) {
//                Struct struct = obj[off + i];
//                long addr = struct == null ? 0 : struct.getMemory().address();
//                long get = array.get(i);
//            }
//        }
//    }

}
