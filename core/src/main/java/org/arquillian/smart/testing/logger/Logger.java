package org.arquillian.smart.testing.logger;

public interface Logger {

    /**
     * Will format the given message with the given arguments and log it at info level.
     *
     * @param msg
     *     Message to print
     * @param args
     *     arguments to use for formatting the given message
     */
    void info(String msg, Object... args);

    /**
     * Will format the given message with the given arguments and log it at warn level.
     *
     * @param msg
     *     Message to print
     * @param args
     *     arguments to use for formatting the given message
     */
    void warn(String msg, Object... args);

    /**
     * Will format the given message with the given arguments and log it at debug level, if debug mode is enabled.
     *
     * @param msg
     *     Message to print
     * @param args
     *     arguments to the message
     */
    void debug(String msg, Object... args);

    /**
     * Will format the given message with the given arguments and log it at error level.
     *
     * @param msg
     *     Message to print
     * @param args
     *     arguments to the message
     */
    void error(String msg, Object... args);

    /**
     * Returns if debug log level is enabled
     *
     * @return
     *     if debug log level is enabled
     */
    boolean isDebug();
}
