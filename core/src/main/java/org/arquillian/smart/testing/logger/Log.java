package org.arquillian.smart.testing.logger;

public class Log {

    private static LoggerFactory loggerFactory;
    private static final DefaultLoggerFactory DEFAULT_LOGGER_FACTORY = new DefaultLoggerFactory();

    private Log(){
    }

    public static void setLoggerFactory(LoggerFactory loggerFactory) {
        synchronized (DEFAULT_LOGGER_FACTORY) {
            Log.loggerFactory = loggerFactory;
            DEFAULT_LOGGER_FACTORY.setDebug(loggerFactory.getLogger().isDebug());
        }
    }

    public static Logger getLogger() {
        synchronized (DEFAULT_LOGGER_FACTORY) {
            if (loggerFactory == null) {
                return DEFAULT_LOGGER_FACTORY.getLogger();
            } else {
                return loggerFactory.getLogger();
            }
        }
    }
}
