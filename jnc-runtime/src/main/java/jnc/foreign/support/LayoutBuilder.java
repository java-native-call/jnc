/*
 * Copyright 2019 zhanhb.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jnc.foreign.support;

import java.util.function.IntFunction;

/**
 * @author zhanhb
 */
public abstract class LayoutBuilder {

    // Don't rely on this variable, maybe changed.
    private static final int MAX_ALIGNMENT = 16;

    private static int alignUp(int size, int alignment) {
        return size + alignment - 1 & -alignment;
    }

    private static int checkPack(int pack) {
        if (pack <= 0 || pack > MAX_ALIGNMENT || (pack & (pack - 1)) != 0) {
            throw new IllegalArgumentException("expect pack to be '1', '2', '4', '8', or '16'");
        }
        return pack;
    }

    public static LayoutBuilder withPack(Type layoutType, int pack) {
        return layoutType.create(checkPack(pack));
    }

    /**
     * Return a builder with maximum alignment supported by the system.
     */
    public static LayoutBuilder withoutPack(Type layoutType) {
        return layoutType.create(MAX_ALIGNMENT);
    }

    private static void checkSizeAndAlignment(int size, int alignment, int addingOffset) {
        if (size < 0) {
            throw new IllegalArgumentException("Illegal size " + size);
        }
        // alignment = Integer.MIN_VALUE? check only `bitCount` is not enough
        if (alignment <= 0 || (alignment & (alignment - 1)) != 0) {
            throw new IllegalArgumentException("Illegal alignment " + alignment);
        }
        if ((size & ~-alignment) != 0) {
            throw new IllegalArgumentException("Illegal combination of size and alignment. size="
                    + size + ", alignment=" + alignment);
        }
        if (size > (Integer.MAX_VALUE & -alignment) - addingOffset) {
            throw new OutOfMemoryError("size too large " + size);
        }
    }

    public final int newField(int size, int alignment) {
        return addFieldImpl(size, alignment);
    }

    public final int size() {
        return sizeImpl();
    }

    public final int alignment() {
        return alignmentImpl();
    }

    abstract int sizeImpl();

    abstract int alignmentImpl();

    abstract int addFieldImpl(int size, int alignment);

    private static final class OfStruct extends LayoutBuilder {

        private final int pack;
        private int offset;
        private int alignment = 1;

        OfStruct(int pack) {
            this.pack = pack;
        }

        @Override
        int addFieldImpl(int size, int alignment) {
            final int thisOffset = this.offset;
            checkSizeAndAlignment(size, alignment, thisOffset);
            final int actualAlignment = Math.min(pack, alignment);
            final int offset = alignUp(thisOffset, actualAlignment);
            this.offset = offset + size;
            this.alignment = Math.max(this.alignment, actualAlignment);
            return offset;
        }

        @Override
        int sizeImpl() {
            return alignUp(offset, alignment);
        }

        @Override
        int alignmentImpl() {
            return alignment;
        }

    }

    private static final class OfUnion extends LayoutBuilder {

        private final int pack;
        private int size;
        private int alignment;

        OfUnion(int pack) {
            this.pack = pack;
        }

        @Override
        int sizeImpl() {
            return size;
        }

        @Override
        int alignmentImpl() {
            return alignment;
        }

        @Override
        int addFieldImpl(int size, int alignment) {
            checkSizeAndAlignment(size, alignment, 0);
            int value = Math.max(this.alignment, Math.min(alignment, pack));
            this.alignment = value;
            this.size = alignUp(Math.max(this.size, size), value);
            return 0;
        }

    }

    @SuppressWarnings("PublicInnerClass")
    public enum Type {
        STRUCT(OfStruct::new),
        UNION(OfUnion::new);
        private final IntFunction<LayoutBuilder> creator;

        Type(IntFunction<LayoutBuilder> function) {
            this.creator = function;
        }

        LayoutBuilder create(int pack) {
            return creator.apply(pack);
        }
    }
}
