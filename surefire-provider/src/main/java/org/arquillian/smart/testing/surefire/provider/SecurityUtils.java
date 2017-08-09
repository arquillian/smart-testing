package org.arquillian.smart.testing.surefire.provider;

import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

class SecurityUtils {

    /**
     * Create a new instance by finding a constructor that matches the argumentTypes signature
     * using the arguments for instantiation.
     *
     * @param implClass
     *     Full classname of class to create
     * @param argumentTypes
     *     The constructor argument types
     * @param arguments
     *     The constructor arguments
     *
     * @return a new instance
     *
     * @throws IllegalArgumentException
     *     if className, argumentTypes, or arguments are null
     * @throws RuntimeException
     *     if any exceptions during creation
     */
    static <T> T newInstance(final Class<T> implClass, final Class<?>[] argumentTypes,
        final Object[] arguments) {
        if (implClass == null) {
            throw new IllegalArgumentException("ImplClass must be specified");
        }
        if (argumentTypes == null) {
            throw new IllegalArgumentException("ArgumentTypes must be specified. Use empty array if no arguments");
        }
        if (arguments == null) {
            throw new IllegalArgumentException("Arguments must be specified. Use empty array if no arguments");
        }
        final T obj;
        try {
            final Constructor<T> constructor = getConstructor(implClass, argumentTypes);
            if (!constructor.isAccessible()) {
                AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                    constructor.setAccessible(true);
                    return null;
                });
            }
            obj = constructor.newInstance(arguments);
        } catch (Exception e) {
            throw new RuntimeException("Could not create new instance of " + implClass, e);
        }

        return obj;
    }

    /**
     * Obtains the Constructor specified from the given Class and argument types
     */
    private static <T> Constructor<T> getConstructor(final Class<T> clazz, final Class<?>... argumentTypes)
        throws NoSuchMethodException {
        try {
            return AccessController.doPrivileged(
                (PrivilegedExceptionAction<Constructor<T>>) () -> clazz.getDeclaredConstructor(argumentTypes));
        }
        // Unwrap
        catch (final PrivilegedActionException pae) {
            final Throwable t = pae.getCause();
            // Rethrow
            if (t instanceof NoSuchMethodException) {
                throw (NoSuchMethodException) t;
            } else {
                // No other checked Exception thrown by Class.getConstructor
                try {
                    throw (RuntimeException) t;
                }
                // Just in case we've really messed up
                catch (final ClassCastException cce) {
                    throw new RuntimeException("Obtained unchecked Exception; this code should never be reached", t);
                }
            }
        }
    }
}
