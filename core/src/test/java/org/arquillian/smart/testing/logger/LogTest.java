package org.arquillian.smart.testing.logger;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
