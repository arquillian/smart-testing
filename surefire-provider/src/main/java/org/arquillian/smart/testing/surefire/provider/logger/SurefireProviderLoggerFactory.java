package org.arquillian.smart.testing.surefire.provider.logger;

import org.apache.maven.surefire.report.ConsoleLogger;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.logger.LoggerFactory;

import static java.lang.String.format;

public class SurefireProviderLoggerFactory implements LoggerFactory {

    private ConsoleLogger logger;
    private boolean debugLogEnabled;

    public SurefireProviderLoggerFactory(ConsoleLogger consoleLogger, boolean debugLogEnabled) {
        this.logger = consoleLogger;
        this.debugLogEnabled = debugLogEnabled;
    }

    @Override
    public Logger getLogger() {
        return new SurefireProviderLogger(logger);
    }

    private class SurefireProviderLogger implements Logger {

        private static final String PREFIX = "%s: Smart Testing Provider - ";

        private ConsoleLogger consoleLogger;

        private SurefireProviderLogger(ConsoleLogger consoleLogger) {
            this.consoleLogger = consoleLogger;
        }

        @Override
        public void info(String msg, Object... args) {
            consoleLogger.info(getFormattedMsg("INFO", msg, args));
        }

        @Override
        public void warn(String msg, Object... args) {
            consoleLogger.info(getFormattedMsg("WARN", msg, args));
        }

        @Override
        public void debug(String msg, Object... args) {
            if(debugLogEnabled) {
                consoleLogger.info(getFormattedMsg("DEBUG", msg, args));
            }
        }

        @Override
        public void error(String msg, Object... args) {
            consoleLogger.info(getFormattedMsg("ERROR", msg, args));
        }

        private String getFormattedMsg(String level, String msg, Object... args) {
            if (args != null && args.length > 0) {
                msg = format(msg, args);
            }
            return format(PREFIX, level) + msg + System.lineSeparator();
        }
    }
}
