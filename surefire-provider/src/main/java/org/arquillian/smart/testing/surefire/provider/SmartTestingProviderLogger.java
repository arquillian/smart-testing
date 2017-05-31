package org.arquillian.smart.testing.surefire.provider;

import static java.lang.String.format;

public class SmartTestingProviderLogger {

    private static SmartTestingProviderLogger logger = null;
    private static String prefix = "%s: Smart-Testing - ";

    private SmartTestingProviderLogger() {
    }

    public static SmartTestingProviderLogger getLogger() {
        if (logger == null) {
            logger = new SmartTestingProviderLogger();
        }
        return logger;
    }

    /**
     * Will format the given message with the given arguments and prints it on standard output with the prefix:
     * "INFO: Smart-Testing - "
     *
     * @param msg Message to print
     * @param args arguments to use for formatting the given message
     */
    public void info(String msg, Object... args) {
        System.out.println(getFormattedMsg("INFO", msg, args));
    }

    /**
     * Will format the given message with the given arguments and prints it on error output with the prefix:
     * "WARN: Smart-Testing - "
     *
     * @param msg Message to print
     * @param args arguments to use for formatting the given message
     */
    public void warn(String msg, Object... args) {
        System.err.println(getFormattedMsg("WARN", msg, args));
    }

    private String getFormattedMsg(String level, String msg, Object... args) {
        if (args != null && args.length > 0){
            msg = format(msg, args);
        }
        return format(prefix, level) + msg;
    }
}
