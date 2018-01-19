package org.arquillian.smart.testing;

import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;

import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static java.util.Arrays.asList;

public class TestSelection {

    private final String className;

    private final Collection<String> appliedStrategies;

    private final Collection<String> testMethodNames;

    public static final TestSelection NOT_MATCHED = new TestSelection("");

    public TestSelection(Path location, String... appliedStrategies) {
        this(new ClassNameExtractor().extractFullyQualifiedName(location), appliedStrategies);
    }

    public TestSelection(String className, String ... appliedStrategies) {
        this(className, new ArrayList<>(), appliedStrategies);
    }

    public TestSelection(String className, Collection<String> testMethodNames, String ... appliedStrategies) {
        this.className = className;
        this.appliedStrategies = new LinkedHashSet<>(asList(appliedStrategies));
        this.testMethodNames = testMethodNames;
    }

    public String getClassName() {
        return className;
    }

    public Collection<String> getAppliedStrategies() {
        return Collections.unmodifiableCollection(appliedStrategies);
    }

    public Collection<String> getTestMethodNames() {
        return Collections.unmodifiableCollection(testMethodNames);
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
        return "TestSelection{" + "className='" + className + '\'' + ", appliedStrategies=" + appliedStrategies + '}';
    }

    public TestSelection merge(TestSelection other) {

        if (!className.equals(other.getClassName())) {
            throw new IllegalArgumentException(
                format("Cannot merge two test selections with different locations (%s != %s)", getClassName(),
                    other.getClassName()));
        }

        final String[] appliedStrategies = getAppliedStrategies().toArray(new String[getAppliedStrategies().size()]);
        final String[] otherAppliedStrategies = other.getAppliedStrategies().toArray(new String[other.getAppliedStrategies().size()]);
        return new TestSelection(getClassName(), concat(appliedStrategies, otherAppliedStrategies));
    }

    private String[] concat(String[] first, String[] second) {
        final String[] result = (String[]) Array.newInstance(String.class, (first.length + second.length));
        arraycopy(first, 0, result, 0, first.length);
        arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
