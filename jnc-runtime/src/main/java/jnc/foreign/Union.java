package jnc.foreign;

import jnc.foreign.internal.NotFinal;

@NotFinal(NotFinal.Reason.API)
@SuppressWarnings("WeakerAccess")
public class Union extends Struct {

    public Union() {
        //noinspection RedundantCast
        super((Void) null);
    }

}
