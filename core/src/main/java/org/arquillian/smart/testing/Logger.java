package org.arquillian.smart.testing;

import java.util.logging.Level;

import static java.lang.String.format;

public class Logger {

    private java.util.logging.Logger jul;

    private static String prefix = "%s: Smart-Testing - ";

    private Logger(java.util.logging.Logger logger) {
        this.jul = logger;
    }

    public static Logger getLogger(Class clazz) {
        return getLogger(clazz.getName());
    }

    public static Logger getLogger(String name) {
        return new Logger(java.util.logging.Logger.getLogger(name));
    }

    /**
     * Log a message using java.util.logging.Logger
     *
     * @param   level
     *     One of the message level identifiers, e.g., SEVERE
     * @param   msg
     *     The string message (or a key in the message catalog)
     * @param   args
     *     arguments to the message
     */
    public void log(Level level, String msg, Object... args) {
        if (jul.isLoggable(level)) {
            jul.log(level, msg, args);
        }
    }

    /**
     * Will format the given message with the given arguments and prints it on standard output with the prefix:
     * "INFO: Smart-Testing - "
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
     * "WARN: Smart-Testing - "
     *
     * @param msg
     *     Message to print
     * @param args
     *     arguments to use for formatting the given message
     */
    public void warn(String msg, Object... args) {
        System.err.println(getFormattedMsg("WARN", msg, args));
    }

    public void severe(String msg) {
        jul.severe(msg);
    }

    public void config(String msg) {
        jul.config(msg);
    }

    public void fine(String msg) {
        jul.fine(msg);
    }

    public void finer(String msg) {
        jul.finer(msg);
    }

    public void finest(String msg) {
        jul.finest(msg);
    }

    private String getFormattedMsg(String level, String msg, Object... args) {
        if (args != null && args.length > 0) {
            msg = format(msg, args);
        }
        return format(prefix, level) + msg;
    }
}
