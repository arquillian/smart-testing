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
     * @param service interface
     * @param <SERVICE>
     * @return Iterable of all services implementing service interface and present in classpath.
     */
    public <SERVICE> Iterable<SERVICE> all(Class<SERVICE> service) {
        return ServiceLoader.load(service);
    }

    /**
     * Get all Java services that implements given interface and meets the given predicate.
     * @param service interface
     * @param predicate to set filtering options
     * @return Iterable of all services implementing service interface, meeting predicate condition and present in classpath.
     */
    public <SERVICE> Iterable<SERVICE> all(Class<SERVICE> service, Predicate<SERVICE> predicate) {
        return StreamSupport.stream(all(service).spliterator(), false).filter(predicate).collect(Collectors.toList());
    }

    /**
     * Get only one service of given type. This method is used when you are sure that only one implementation of given service is in classpath.
     * If there are more than one, then {@link IllegalStateException} is thrown.
     * @param service interface
     * @return The service of given type (if there are any) or an exception in case of more than one
     */
    public <SERVICE> Optional<SERVICE> onlyOne(Class<SERVICE> service) {
        Iterable<SERVICE> all = all(service);
        return getOnlyOneServiceFromIterable(service, all);
    }

    /**
     * Get only one service of given type. This method is used when you want to filter from all possible implementations of given service.
     * If there are more than one, then {@link IllegalStateException} is thrown.
     * @param service interface
     * @param predicate to set filtering options
     * @return The service of given type and meeting predicate condition (if any) or an exception in case of more than one.
     */
    public <SERVICE> Optional<SERVICE> onlyOne(Class<SERVICE> service, Predicate<SERVICE> predicate) {
        final Iterable<SERVICE> all = all(service, predicate);
        return getOnlyOneServiceFromIterable(service, all);
    }

    private <SERVICE> Optional<SERVICE> getOnlyOneServiceFromIterable(Class<SERVICE> service, Iterable<SERVICE> all) {
        final Iterator<SERVICE> allIterator = all.iterator();
        if (allIterator.hasNext()) {
            SERVICE serviceInstance =  allIterator.next();

            if (allIterator.hasNext()) {
                throw new IllegalStateException(
                    "Multiple service implementations found for " + service + ". " + toClassString(all));
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
