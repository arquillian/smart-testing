package org.arquillian.smart.testing;

import static java.lang.String.format;
import static org.arquillian.smart.testing.Configuration.SMART_TESTING_DEBUG;

public class Logger {

    private static final String PREFIX = "[%s] [Smart Testing Extension] ";

    private boolean mavenDebugLogLevel = false;

    public static Logger getLogger() {
        return new Logger();
    }

    /**
     * Will format the given message with the given arguments and prints it on standard output with the prefix:
     * "[INFO] [Smart Testing Extension]"
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
     * "[WARN] [Smart Testing Extension]"
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
     * "[DEBUG] [Smart Testing Extension]", if debug mode is enabled.
     *
     * @param msg
     *     The string message (or a key in the message catalog)
     * @param args
     *     arguments to the message
     */
    public void debug(String msg, Object... args) {
        if (isDebugLogLevelEnabled()) {
            System.out.println(getFormattedMsg("DEBUG", msg, args));
        }
    }

    /**
     * Will format the given message with the given arguments and prints it on standard output with the prefix:
     * "[ERROR] [Smart Testing Extension]", if debug mode is enabled.
     *
     * @param msg
     *     The string message (or a key in the message catalog)
     * @param args
     *     arguments to the message
     */
    public void error(String msg, Object... args) {
        System.err.println(getFormattedMsg("ERROR", msg, args));
    }

    public Boolean isDebugLogLevelEnabled() {
        return Boolean.valueOf(System.getProperty(SMART_TESTING_DEBUG, "false")) || mavenDebugLogLevel;
    }

    public void enableMavenDebugLogLevel(Boolean mavenDebugLevel) {
        mavenDebugLogLevel = mavenDebugLevel;
    }

    private String getFormattedMsg(String level, String msg, Object... args) {
        if (args != null && args.length > 0) {
            msg = format(msg, args);
        }
        return format(PREFIX, level) + msg;
    }
}
