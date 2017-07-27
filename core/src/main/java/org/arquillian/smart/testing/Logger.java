package org.arquillian.smart.testing;

import java.util.logging.Level;

import static java.lang.String.format;

public class Logger {

    private java.util.logging.Logger jul;

    private static String prefix = "%s: Smart-Testing - ";

    private Logger(java.util.logging.Logger logger) {
        this.jul = logger;
    }

    /**
     * Find or create a logger for a named subsystem.
     *
     * @param cls
     *     A class name of the subsystem for the logger such as java.net.URI or javax.swing.Box
     *
     * @return a suitable Logger
     */
    public static Logger getLogger(Class cls) {
        return getLogger(cls.getName());
    }

    /**
     * Delegates creation of a logger for a named subsystem to java.util.logging.Logger.
     *
     * @param name
     *     A name for the logger.  This should be a dot-separated name and should normally
     *     be based on the package name or class name of the subsystem, such as java.net
     *     or javax.swing
     *
     * @return a suitable Logger
     *
     * @throws NullPointerException
     *     if the name is null.
     */
    public static Logger getLogger(String name) {
        return new Logger(java.util.logging.Logger.getLogger(name));
    }

    /**
     * Log a formatted message with given arguments using java.util.logging.Logger
     *
     * @param level
     *     One of the message level identifiers, e.g., SEVERE
     * @param msg
     *     The string message (or a key in the message catalog)
     * @param args
     *     arguments to the message
     */
    public void log(Level level, String msg, Object... args) {
        if (jul.isLoggable(level)) {
            jul.log(level, getFormattedMsg(msg, args));
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

    /**
     * Log a formatted SEVERE message with given arguments using java.util.logging.Logger
     *
     * @param msg
     *     The string message (or a key in the message catalog)
     * @param args
     *     arguments to the message
     */
    public void severe(String msg, Object... args) {
        jul.severe(getFormattedMsg(msg, args));
    }

    /**
     * Log a formatted CONFIG message with given arguments using java.util.logging.Logger
     *
     * @param msg
     *     The string message (or a key in the message catalog)
     * @param args
     *     arguments to the message
     */
    public void config(String msg, Object... args) {
        jul.config(getFormattedMsg(msg, args));
    }

    /**
     * Log a formatted FINE message with given arguments using java.util.logging.Logger
     *
     * @param msg
     *     The string message (or a key in the message catalog)
     * @param args
     *     arguments to the message
     */
    public void fine(String msg, Object... args) {
        jul.fine(getFormattedMsg(msg, args));
    }

    /**
     * Log a formatted FINER message with given arguments using java.util.logging.Logger
     *
     * @param msg
     *     The string message (or a key in the message catalog)
     * @param args
     *     arguments to the message
     */
    public void finer(String msg, Object... args) {
        jul.finer(getFormattedMsg(msg, args));
    }

    /**
     * Log a formatted FINEST message with given arguments using java.util.logging.Logger
     *
     * @param msg
     *     The string message (or a key in the message catalog)
     * @param args
     *     arguments to the message
     */
    public void finest(String msg, Object... args) {
        jul.finest(getFormattedMsg(msg, args));
    }

    private String getFormattedMsg(String msg, Object... args) {
        if (args != null && args.length > 0) {
            msg = format(msg, args);
        }
        return msg;
    }

    private String getFormattedMsg(String level, String msg, Object... args) {
        if (args != null && args.length > 0) {
            msg = format(msg, args);
        }
        return format(prefix, level) + msg;
    }
}
