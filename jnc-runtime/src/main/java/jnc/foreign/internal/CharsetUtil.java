package jnc.foreign.internal;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
final class CharsetUtil {

    private static final ConcurrentHashMap<Charset, Integer> TERMINATOR_LENGTH_CACHE = new ConcurrentHashMap<>(4);
    private static final Charset COMPOUND_TEXT;

    static {
        Charset compoundText = null;
        try {
            compoundText = Charset.forName("COMPOUND_TEXT");
        } catch (UnsupportedCharsetException ex) {
            // Charset COMPOUND_TEXT is not support on this jdk
            // we are happy to not handle this charset.
        }
        COMPOUND_TEXT = compoundText;
    }

    private static boolean isTerminatorLengthEquals(Charset charset, int expect) {
        try {
            return charset.newDecoder().decode(ByteBuffer.allocate(expect))
                    .toString().equals("\u0000");
        } catch (CharacterCodingException ex) {
            // TODO catch CharacterCodingException not enough? user installed charset?
            return false;
        }
    }

    static int getTerminatorLength(Charset charset) {
        Objects.requireNonNull(charset);
        return TERMINATOR_LENGTH_CACHE.computeIfAbsent(charset, ch -> {
            for (int maybe : new int[]{1, 2, 4}) {
                if (isTerminatorLengthEquals(ch, maybe)) {
                    return maybe;
                }
            }
            // Null character is not defined in these charsets
            // COMPOUND_TEXT, JIS_X0212-1990, IBM300, IBM834, JIS0208
            // Except COMPOUND_TEXT all others are fixed length charsets with
            // byte count per char 2. For COMPOUND_TEXT, let's assume length
            // of null terminated character is 1.
            // assume terminator length is 2 for all other unknown charsets.

            // COMPOUND_TEXT can be null
            return ch.equals(COMPOUND_TEXT) ? 1 : 2;
        });
    }

}
