package jnc.foreign;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForeignProvidersTest {

    private static final Logger log = LoggerFactory.getLogger(ForeignProvidersTest.class);

    /**
     * Test of getDefault method, of class ForeignProviders.
     */
    @Test
    public void testGetDefault() {
        log.info("getDefault");
        ForeignProviders.getDefault();
    }

    @Test
    public void testNewInstance() throws Exception {
        log.info("new ForeignProviders");
        Constructor<ForeignProviders> constructor = ForeignProviders.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(AssertionError.class);
    }

}
