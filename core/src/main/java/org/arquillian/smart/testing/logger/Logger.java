package org.arquillian.smart.testing.logger;

public interface Logger {

    /**
     * Will format the given message with the given arguments and prints it on standard output with the prefix:
     * "[INFO] [Smart Testing Extension]"
     *
     * @param msg
     *     Message to print
     * @param args
     *     arguments to use for formatting the given message
     */
    void info(String msg, Object... args);

    /**
     * Will format the given message with the given arguments and prints it on error output with the prefix:
     * "[WARN] [Smart Testing Extension]"
     *
     * @param msg
     *     Message to print
     * @param args
     *     arguments to use for formatting the given message
     */
    void warn(String msg, Object... args);

    /**
     * Will format the given message with the given arguments and prints it on standard output with the prefix:
     * "[DEBUG] [Smart Testing Extension]", if debug mode is enabled.
     *
     * @param msg
     *     The string message (or a key in the message catalog)
     * @param args
     *     arguments to the message
     */
    void debug(String msg, Object... args);

    /**
     * Will format the given message with the given arguments and prints it on standard output with the prefix:
     * "[ERROR] [Smart Testing Extension]", if debug mode is enabled.
     *
     * @param msg
     *     The string message (or a key in the message catalog)
     * @param args
     *     arguments to the message
     */
    void error(String msg, Object... args);
}
