package org.arquillian.smart.testing.strategies.affected;

import java.util.ArrayList;
import java.util.List;
import org.arquillian.smart.testing.strategies.affected.ast.JavaAssistClassParser;
import org.arquillian.smart.testing.strategies.affected.ast.JavaClass;
import org.arquillian.smart.testing.strategies.affected.fakeproject.main.A;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ComponentUnderTestResolverTest {

    @Test
    public void should_resolve_classes_from_component_under_test_annotation() {

        // given
        ComponentUnderTestResolver componentUnderTestResolver = new ComponentUnderTestResolver();
        final JavaAssistClassParser javaAssistClassParser = new JavaAssistClassParser();
        final JavaClass testClass = javaAssistClassParser.getClass(ClassesAsComponentUnderTestTest.class.getName());

        // when
        final List<String> imports = componentUnderTestResolver.resolve(testClass);

        // then
        assertThat(imports)
            .containsExactlyInAnyOrder("java.lang.String");
    }

    @Test
    public void should_resolve_packages_from_component_under_test_annotation() {

        // given
        ComponentUnderTestResolver componentUnderTestResolver = new ComponentUnderTestResolver();
        final JavaAssistClassParser javaAssistClassParser = new JavaAssistClassParser();
        final JavaClass testClass = javaAssistClassParser.getClass(PackagesAsComponentUnderTestTest.class.getName());

        // when
        final List<String> imports = componentUnderTestResolver.resolve(testClass);

        // then
        assertThat(imports)
            .containsExactlyInAnyOrder("org.arquillian.smart.testing.strategies.affected.fakeproject.main.A",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.main.B",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.main.C",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.main.D",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.main.MyBusinessObject",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.main.MyControllerObject");

    }

    @Test
    public void should_resolve_packages_of_from_component_under_test_annotation() {

        // given
        ComponentUnderTestResolver componentUnderTestResolver = new ComponentUnderTestResolver();
        final JavaAssistClassParser javaAssistClassParser = new JavaAssistClassParser();
        final JavaClass testClass = javaAssistClassParser.getClass(PackagesOfAsComponentUnderTestTest.class.getName());

        // when
        final List<String> imports = componentUnderTestResolver.resolve(testClass);

        // then
        assertThat(imports)
            .containsExactlyInAnyOrder("org.arquillian.smart.testing.strategies.affected.fakeproject.main.A",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.main.B",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.main.C",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.main.D",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.main.MyBusinessObject",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.main.MyControllerObject");

    }

    @Test
    public void should_append_classes_and_packages_of_from_component_under_test_annotation() {

        // given
        ComponentUnderTestResolver componentUnderTestResolver = new ComponentUnderTestResolver();
        final JavaAssistClassParser javaAssistClassParser = new JavaAssistClassParser();
        final JavaClass testClass = javaAssistClassParser.getClass(AppendResultPackagesOfAndClassesAsComponentUnderTestTest.class.getName());

        // when
        final List<String> imports = componentUnderTestResolver.resolve(testClass);

        // then
        assertThat(imports)
            .containsExactlyInAnyOrder("org.arquillian.smart.testing.strategies.affected.fakeproject.main.A",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.main.B",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.main.C",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.main.D",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.main.MyBusinessObject",
                "org.arquillian.smart.testing.strategies.affected.fakeproject.main.MyControllerObject",
                "java.lang.String");
    }

    @Test
    public void should_resolve_classes_from_multiple_component_under_test_annotation() {

        // given
        ComponentUnderTestResolver componentUnderTestResolver = new ComponentUnderTestResolver();
        final JavaAssistClassParser javaAssistClassParser = new JavaAssistClassParser();
        final JavaClass testClass = javaAssistClassParser.getClass(MultipleClassesAsComponentUnderTestTest.class.getName());

        // when
        final List<String> imports = componentUnderTestResolver.resolve(testClass);

        // then
        assertThat(imports)
            .containsExactlyInAnyOrder("java.lang.String", "java.util.ArrayList");
    }

    @Test
    public void should_resolve_classes_from_multiple_components_under_test_annotation() {

        // given
        ComponentUnderTestResolver componentUnderTestResolver = new ComponentUnderTestResolver();
        final JavaAssistClassParser javaAssistClassParser = new JavaAssistClassParser();
        final JavaClass testClass = javaAssistClassParser.getClass(MultipleClassesAsComponentsUnderTestTest.class.getName());

        // when
        final List<String> imports = componentUnderTestResolver.resolve(testClass);

        // then
        assertThat(imports)
            .containsExactlyInAnyOrder("java.lang.String", "java.util.ArrayList");
    }

    @Ignore("Test ignored because it is used internally")
    @ComponentsUnderTest({
        @ComponentUnderTest(classes = String.class),
        @ComponentUnderTest(classes = ArrayList.class)
    })
    public static class MultipleClassesAsComponentsUnderTestTest {
    }

    @Ignore("Test ignored because it is used internally")
    @ComponentUnderTest(classes = String.class)
    @ComponentUnderTest(classes = ArrayList.class)
    public static class MultipleClassesAsComponentUnderTestTest {
    }

    @Ignore("Test ignored because it is used internally")
    @ComponentUnderTest(packagesOf = A.class, classes = String.class)
    public static class AppendResultPackagesOfAndClassesAsComponentUnderTestTest {
    }

    @Ignore("Test ignored because it is used internally")
    @ComponentUnderTest(packagesOf = A.class)
    public static class PackagesOfAsComponentUnderTestTest {
    }

    @Ignore("Test ignored because it is used internally")
    @ComponentUnderTest(packages = "org.arquillian.smart.testing.strategies.affected.fakeproject.main")
    public static class PackagesAsComponentUnderTestTest {
    }

    @Ignore("Test ignored because it is used internally")
    @ComponentUnderTest(classes = String.class)
    public static class ClassesAsComponentUnderTestTest {
    }
}
