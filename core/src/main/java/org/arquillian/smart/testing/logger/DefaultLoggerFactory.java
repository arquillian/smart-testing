package org.arquillian.smart.testing.logger;

import org.arquillian.smart.testing.configuration.Configuration;

import static java.lang.String.format;

public class DefaultLoggerFactory implements LoggerFactory {

    private boolean isDebugLogLevel;

    public DefaultLoggerFactory(boolean isDebugLogLevel) {
        this.isDebugLogLevel = isDebugLogLevel;
    }

    public DefaultLoggerFactory() {
        this.isDebugLogLevel = Boolean.valueOf(System.getProperty(Configuration.SMART_TESTING_DEBUG));
    }

    @Override
    public Logger getLogger() {
        return new DefaultLogger(isDebugLogLevel);
    }

    void setDebug(boolean isDebugLogLevel) {
        this.isDebugLogLevel = isDebugLogLevel;
    }

    private class DefaultLogger implements Logger {

        private static final String PREFIX = "%s: Smart Testing Extension - ";

        private boolean isDebugLogLevel;

        DefaultLogger(boolean isDebugLogLevel) {
            this.isDebugLogLevel = isDebugLogLevel;
        }

        /**
         * Will format the given message with the given arguments and prints it on standard output with the prefix:
         * "INFO: Smart Testing Extension -"
         *
         * @param msg
         *     Message to print
         * @param args
         *     arguments to use for formatting the given message
         */
        public void info(String msg, Object... args) {
            System.out.println(getFormattedMsg("INFO", msg, args));
        }

        /**
         * Will format the given message with the given arguments and prints it on error output with the prefix:
         * "WARN: Smart Testing Extension -"
         *
         * @param msg
         *     Message to print
         * @param args
         *     arguments to use for formatting the given message
         */
        public void warn(String msg, Object... args) {
            System.err.println(getFormattedMsg("WARN", msg, args));
        }

        /**
         * Will format the given message with the given arguments and prints it on standard output with the prefix:
         * "DEBUG: Smart Testing Extension -", if debug mode is enabled.
         *
         * @param msg
         *     The string message (or a key in the message catalog)
         * @param args
         *     arguments to the message
         */
        public void debug(String msg, Object... args) {
            if (isDebugLogLevel) {
                System.out.println(getFormattedMsg("DEBUG", msg, args));
            }
        }

        /**
         * Will format the given message with the given arguments and prints it on standard output with the prefix:
         * "ERROR: Smart Testing Extension", if debug mode is enabled.
         *
         * @param msg
         *     The string message (or a key in the message catalog)
         * @param args
         *     arguments to the message
         */
        public void error(String msg, Object... args) {
            System.err.println(getFormattedMsg("ERROR", msg, args));
        }

        @Override
        public boolean isDebug() {
            return isDebugLogLevel;
        }

        private String getFormattedMsg(String level, String msg, Object... args) {
            if (args != null && args.length > 0) {
                msg = format(msg, args);
            }
            return format(PREFIX, level) + msg;
        }
    }
}
