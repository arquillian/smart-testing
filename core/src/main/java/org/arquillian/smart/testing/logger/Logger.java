package org.arquillian.smart.testing.logger;

public interface Logger {

    /**
     * Will format the given message with the given arguments and print it on standard output.
     *
     * @param msg
     *     Message to print
     * @param args
     *     arguments to use for formatting the given message
     */
    void info(String msg, Object... args);

    /**
     * Will format the given message with the given arguments and print it on standard output.
     *
     * @param msg
     *     Message to print
     * @param args
     *     arguments to use for formatting the given message
     */
    void warn(String msg, Object... args);

    /**
     * Will format the given message with the given arguments and print it on standard output, if debug mode is enabled.
     *
     * @param msg
     *     The string message (or a key in the message catalog)
     * @param args
     *     arguments to the message
     */
    void debug(String msg, Object... args);

    /**
     * Will format the given message with the given arguments and print it on standard output.
     *
     * @param msg
     *     The string message (or a key in the message catalog)
     * @param args
     *     arguments to the message
     */
    void error(String msg, Object... args);
}
