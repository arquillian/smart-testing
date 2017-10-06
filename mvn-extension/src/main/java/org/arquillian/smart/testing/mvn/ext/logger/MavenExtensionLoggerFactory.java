package org.arquillian.smart.testing.mvn.ext.logger;

import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.logger.DefaultLoggerFactory;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.logger.LoggerFactory;

public class MavenExtensionLoggerFactory implements LoggerFactory {

    private final org.codehaus.plexus.logging.Logger logger;
    private Configuration configuration;

    public MavenExtensionLoggerFactory(org.codehaus.plexus.logging.Logger logger, Configuration configuration) {
        this.logger = logger;
        this.configuration = configuration;
    }

    public MavenExtensionLoggerFactory(org.codehaus.plexus.logging.Logger logger) {
        this.logger = logger;
    }

    @Override
    public Logger getLogger() {
        return new MavenExtensionLogger(logger, configuration);
    }

    private class MavenExtensionLogger implements Logger {

        private static final String SMART_TESTING_EXTENSION_PREFIX = "Smart Testing Extension - ";
        private final org.codehaus.plexus.logging.Logger logger;
        private Configuration configuration;

        MavenExtensionLogger(org.codehaus.plexus.logging.Logger logger, Configuration configuration) {
            this.logger = logger;
            this.configuration = configuration;
        }

        @Override
        public void info(String msg, Object... args) {
            logger.info(getFormattedMsg(msg, args));
        }

        @Override
        public void warn(String msg, Object... args) {
            logger.warn(getFormattedMsg(msg, args));
        }

        @Override
        public void debug(String msg, Object... args) {
            logger.debug(getFormattedMsg(msg, args));
            if (configuration != null && configuration.isDebug() && !logger.isDebugEnabled()) {
                new DefaultLoggerFactory(true).getLogger().debug(msg, args);
            }
        }

        @Override
        public void error(String msg, Object... args) {
            logger.error(getFormattedMsg(msg, args));
        }

        @Override
        public boolean isDebug() {
            return logger.isDebugEnabled() || (configuration != null && configuration.isDebug())
                   || Boolean.valueOf(System.getProperty(Configuration.SMART_TESTING_DEBUG));
        }

        private String getFormattedMsg(String msg, Object... args) {
            return SMART_TESTING_EXTENSION_PREFIX + String.format(msg, args);
        }
    }
}
