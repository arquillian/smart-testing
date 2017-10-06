package org.arquillian.smart.testing.logger;

public class DummyLoggerFactory implements LoggerFactory {

    @Override
    public Logger getLogger() {
        return new DummyLogger();
    }

    private class DummyLogger implements Logger {

        @Override
        public void info(String msg, Object... args) {

        }

        @Override
        public void warn(String msg, Object... args) {

        }

        @Override
        public void debug(String msg, Object... args) {

        }

        @Override
        public void error(String msg, Object... args) {

        }

        @Override
        public boolean isDebug() {
            return false;
        }
    }
}
