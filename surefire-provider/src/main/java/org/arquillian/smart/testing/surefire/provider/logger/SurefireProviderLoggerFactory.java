package org.arquillian.smart.testing.surefire.provider.logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import org.arquillian.smart.testing.logger.DefaultLoggerFactory;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.logger.LoggerFactory;

import static java.lang.String.format;

public class SurefireProviderLoggerFactory implements LoggerFactory {

    private Object logger;
    private boolean debugLogEnabled;
    public static final String NOT_COMPATIBLE_MESSAGE = "The Surefire version that you are using is not fully compatible "
        + "with the current version of Smart Testing. Please create an issue.";

    public SurefireProviderLoggerFactory(Object consoleLogger, boolean debugLogEnabled) {
        this.logger = consoleLogger;
        this.debugLogEnabled = debugLogEnabled;
    }

    @Override
    public Logger getLogger() {
        return new SurefireProviderLogger(logger);
    }

    private class SurefireProviderLogger implements Logger {

        private static final String PREFIX = "%s: Smart Testing Extension - ";

        private Object consoleLogger;
        private Method logMethod;

        private SurefireProviderLogger(Object consoleLogger) {
            this.consoleLogger = consoleLogger;
            this.logMethod = getLogMethod();
        }

        @Override
        public void info(String msg, Object... args) {
            logMessage(getFormattedMsg("INFO", msg, args));
        }

        @Override
        public void warn(String msg, Object... args) {
            logMessage(getFormattedMsg("WARN", msg, args));
        }

        @Override
        public void debug(String msg, Object... args) {
            if (debugLogEnabled) {
                logMessage(getFormattedMsg("DEBUG", msg, args));
            }
        }

        @Override
        public void error(String msg, Object... args) {
            logMessage(getFormattedMsg("ERROR", msg, args));
        }

        private void logMessage(String message) {
            if (logMethod != null) {
                try {
                    logMethod.invoke(consoleLogger, message);
                    return;
                } catch (IllegalAccessException | InvocationTargetException e) {
                }
            }
            System.out.println(message);
        }

        private Method getLogMethod() {
            if (consoleLogger != null) {
                Optional<Method> method = Arrays.stream(consoleLogger.getClass().getMethods())
                    .filter(this::isPublicConsoleLogMethod)
                    .findFirst();

                if (method.isPresent()) {
                    return method.get();
                } else {
                    new DefaultLoggerFactory()
                        .getLogger()
                        .warn(NOT_COMPATIBLE_MESSAGE);
                }
            }
            return null;
        }

        private boolean isPublicConsoleLogMethod(Method method){
            return method.getParameterCount() == 1
                && method.getParameterTypes()[0] == String.class
                && method.getModifiers() == Modifier.PUBLIC;
        }

        private String getFormattedMsg(String level, String msg, Object... args) {
            if (args != null && args.length > 0) {
                msg = format(msg, args);
            }
            StringBuffer formattedMsg = new StringBuffer(format(PREFIX, level)).append(msg);
            if (consoleLogger != null && consoleLogger.getClass().getName().equals("org.apache.maven.surefire.booter.ForkingRunListener")) {
                return formattedMsg.append(System.lineSeparator()).toString();
            }
            return formattedMsg.toString();
        }

        @Override
        public boolean isDebug() {
            return debugLogEnabled;
        }
    }
}
