package org.arquillian.smart.testing.logger;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.assertj.core.api.Assertions.assertThat;

@Category(NotThreadSafe.class)
public class LogTest {

    @Test
    public void should_return_instance_of_default_logger_when_no_logger_factory_set() throws Exception {
        // given
        final Logger logger = Log.getLogger();

        // then
        assertThat(logger).isInstanceOf(new DefaultLoggerFactory().getLogger().getClass());
    }

    @Test
    public void should_return_logger_instance_of_set_logger_factory() throws Exception {
        // given
        Log.setLoggerFactory(new DummyLoggerFactory());

        //when
        final Logger logger = Log.getLogger();

        // then
        assertThat(logger).isInstanceOf(new DummyLoggerFactory().getLogger().getClass());
    }

    @Test
    public void should_reset_logger_instance_to_default_when_logger_factory_reset() throws Exception {
        // given
        Log.setLoggerFactory(new DummyLoggerFactory());

        //when
        Log.setLoggerFactory(new DefaultLoggerFactory());
        final Logger logger = Log.getLogger();

        // then
        assertThat(logger).isInstanceOf(new DefaultLoggerFactory().getLogger().getClass());
    }
}
