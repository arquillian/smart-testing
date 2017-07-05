package org.arquillian.smart.testing.spi;

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class JavaSPILoader {

    public JavaSPILoader() {
    }

    /**
     * Get all Java services that implements given interface.
     * @param serviceType interface
     * @return Iterable of all services implementing serviceType interface and present in classpath.
     */
    public <SERVICE> Iterable<SERVICE> all(Class<SERVICE> serviceType) {
        return ServiceLoader.load(serviceType);
    }

    /**
     * Get all Java services that implements given interface and meets the given predicate.
     * @param serviceType interface
     * @param predicate to set filtering options
     * @return Iterable of all services implementing serviceType interface, meeting predicate condition and present in classpath.
     */
    public <SERVICE> Iterable<SERVICE> all(Class<SERVICE> serviceType, Predicate<SERVICE> predicate) {
        return StreamSupport.stream(all(serviceType).spliterator(), false).filter(predicate).collect(Collectors.toList());
    }

    /**
     * Get only one serviceType of given type. This method is used when you are sure that only one implementation of given serviceType is in classpath.
     * If there are more than one, then {@link IllegalStateException} is thrown.
     * @param serviceType interface
     * @return The serviceType of given type (if there are any) or an exception in case of more than one
     */
    public <SERVICE> Optional<SERVICE> onlyOne(Class<SERVICE> serviceType) {
        Iterable<SERVICE> all = all(serviceType);
        return ensureOnlyOneServiceLoaded(serviceType, all);
    }

    /**
     * Get only one serviceType of given type. This method is used when you want to filter from all possible implementations of given serviceType.
     * If there are more than one, then {@link IllegalStateException} is thrown.
     * @param serviceType interface
     * @param predicate to set filtering options
     * @return The serviceType of given type and meeting predicate condition (if any) or an exception in case of more than one.
     */
    public <SERVICE> Optional<SERVICE> onlyOne(Class<SERVICE> serviceType, Predicate<SERVICE> predicate) {
        final Iterable<SERVICE> all = all(serviceType, predicate);
        return ensureOnlyOneServiceLoaded(serviceType, all);
    }

    private <SERVICE> Optional<SERVICE> ensureOnlyOneServiceLoaded(Class<SERVICE> serviceType, Iterable<SERVICE> all) {
        final Iterator<SERVICE> allIterator = all.iterator();
        if (allIterator.hasNext()) {
            SERVICE serviceInstance =  allIterator.next();

            if (allIterator.hasNext()) {
                throw new IllegalStateException(
                    "Multiple serviceType implementations found for " + serviceType + ". " + toClassString(all));
            }

            return Optional.of(serviceInstance);
        } else {
            return Optional.empty();
        }
    }

    private <SERVICE> String toClassString(Iterable<SERVICE> providers) {
        StringBuilder sb = new StringBuilder();
        for (Object provider : providers) {
            sb.append(provider.getClass().getName()).append(", ");
        }
        return sb.subSequence(0, sb.length() - 2).toString();
    }
}
