package org.arquillian.smart.testing;

import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;

import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static java.util.Arrays.asList;

public class TestSelection {

    private final String className;

    private final Collection<String> types; // TODO or strategy instead (misleading name)

    public TestSelection(Path location, String... type) {
        this(new ClassNameExtractor().extractFullyQualifiedName(location), type);
    }

    public TestSelection(String className, String ... type) {
        this.className = className;
        this.types = new LinkedHashSet<>(asList(type));
    }

    public String getClassName() {
        return className;
    }

    public Collection<String> getTypes() {
        return types; // TODO should we return clone to avoid manipulation?
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TestSelection that = (TestSelection) o;
        return Objects.equals(getClassName(), that.getClassName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClassName());
    }

    @Override
    public String toString() {
        return "TestSelection{" + "className='" + className + '\'' + ", types=" + types + '}';
    }

    public TestSelection merge(TestSelection other) {

        if (!className.equals(other.getClassName())) {
            throw new IllegalArgumentException(
                format("Cannot merge two test selections with different locations (%s != %s)", getClassName(),
                    other.getClassName()));
        }

        final String[] types = getTypes().toArray(new String[getTypes().size()]);
        final String[] typesOfOther = other.getTypes().toArray(new String[other.getTypes().size()]);
        return new TestSelection(getClassName(), concat(types, typesOfOther));
    }

    private String[] concat(String[] first, String[] second) {
        final String[] result = (String[]) Array.newInstance(String.class, (first.length + second.length));
        arraycopy(first, 0, result, 0, first.length);
        arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
