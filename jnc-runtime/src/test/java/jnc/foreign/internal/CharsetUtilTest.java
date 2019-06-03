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
package jnc.foreign.internal;

import java.nio.charset.Charset;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhanhb
 */
public class CharsetUtilTest {

    private static final Logger log = LoggerFactory.getLogger(CharsetUtilTest.class);

    /**
     * Test of getTerminatorLength method, of class CharsetUtil.
     */
    @Test
    public void testGetTerminatorLength() {
        // make sure all charsets on the jvm we are supported
        for (Charset charset : Charset.availableCharsets().values()) {
            int terminatorLength = CharsetUtil.getTerminatorLength(charset);
            if (terminatorLength > 1) {
                // The following characters doesn't define null character, here we log all results
                // JIS_X0212-1990, IBM300, IBM834, JIS0208 and are fixed length charsets.
                // we test all charset if they are fixed length charsets.
                // UTF-16 group charsets will encode non BMP character to 4 bytes and BMP 2 bytes. UTF-32 is familiar.
                // Just test the average bytes per char and max bytes per char are integral

                // assume are charsets with terminator length greater than 1 can encode.
                assertTrue(charset.name(), charset.canEncode());
                assertIsIntegral(charset, "average bytes per char", charset.newEncoder().averageBytesPerChar() / terminatorLength);
                assertIsIntegral(charset, "max bytes per char", charset.newEncoder().maxBytesPerChar() / terminatorLength);
            }
            log.info("{} {}", charset, terminatorLength);
        }
    }

    private void assertIsIntegral(Charset charset, String name, float v) {
        double floor = Math.floor(v + 0.5);
        assertEquals(charset + "." + name + " is not integral", floor, v, 1e-5);
    }

}
