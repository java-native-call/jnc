package jnc.foreign;

import javax.annotation.Nonnull;

public interface ForeignProvider {

    @Nonnull
    Foreign getForeign();

}
