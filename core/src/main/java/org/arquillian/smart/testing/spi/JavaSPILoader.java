package org.arquillian.smart.testing.spi;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JavaSPILoader {

    public JavaSPILoader() {
    }

    public <S> Iterable<S> all(Class<S> service) {
        return ServiceLoader.load(service);
    }

    public <S> Collection<S> all(Class<S> service, Predicate<S> predicate) {
        return StreamSupport.stream(all(service).spliterator(), false).filter(predicate).collect(Collectors.toList());
    }

    public <S> Optional<S> onlyOne(Class<S> service) {
        Iterable<S> all = all(service);

        final Iterator<S> allIterator = all.iterator();
        if (allIterator.hasNext()) {
            S serviceInstance = allIterator.next();

            if (allIterator.hasNext()) {
                throw new IllegalStateException(
                    "Multiple service implementations found for " + service + ". " + toClassString(all));
            }

            return Optional.of(serviceInstance);
        } else {
            return Optional.empty();
        }
    }

    public <S> Optional<S> onlyOne(Class<S> service, Predicate<S> predicate) {
        final Collection<S> all = all(service, predicate);

        if (all.size() == 1) {
            return Optional.of(all.iterator().next());
        }

        if (all.size() > 1) {
            throw new IllegalStateException(
                "Multiple service implementations found for " + service + ". " + toClassString(all));
        }

        return Optional.empty();
    }

    private <S> String toClassString(Iterable<S> providers) {
        StringBuilder sb = new StringBuilder();
        for (Object provider : providers) {
            sb.append(provider.getClass().getName()).append(", ");
        }
        return sb.subSequence(0, sb.length() - 2).toString();
    }
}
