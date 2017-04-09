package org.arquillian.smart.testing.strategies.affected;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaAssistClassParserTest {

    @Test
    public void should_resolve_direct_imports() {
        // given
        final JavaAssistClassParser javaAssistClassParser = new JavaAssistClassParser("");

        // when
        final JavaClass simpleImportsClass =
            javaAssistClassParser.getClass(SimpleImportsClass.class.getName());

        // then
        assertThat(simpleImportsClass.getImports())
            .containsExactlyInAnyOrder("java.util.Arrays", "java.net.MalformedURLException", "java.net.URL",
                "org.arquillian.smart.testing.strategies.affected.SimpleImportsClass", "java.lang.Object",
                "java.util.ArrayList");
    }

    @Test
    public void should_resolve_different_packages_with_same_class_name_fields() {
        // given
        final JavaAssistClassParser javaAssistClassParser = new JavaAssistClassParser("");

        // when
        final JavaClass duplicatedClass =
            javaAssistClassParser.getClass(DuplicateClassNameDifferentPackagesAsField.class.getName());

        // then
        assertThat(duplicatedClass.getImports())
            .containsExactlyInAnyOrder("java.nio.file.Files",
                "org.arquillian.smart.testing.strategies.affected.DuplicateClassNameDifferentPackagesAsField",
                "java.lang.Object", "org.assertj.core.util.Files", "org.arquillian.smart.testing.Files");
    }

    @Test
    public void should_resolve_different_packages_with_same_class_name_local_variables() {
        // given
        final JavaAssistClassParser javaAssistClassParser = new JavaAssistClassParser("");

        // when
        final JavaClass duplicatedClass =
            javaAssistClassParser.getClass(DuplicateClassNameDifferentPackagesAsLocal.class.getName());

        // then
        assertThat(duplicatedClass.getImports())
            .containsExactlyInAnyOrder("java.nio.file.Paths", "java.nio.file.Files",
                "java.lang.Object", "java.io.IOException", "java.lang.String",
                "org.arquillian.smart.testing.strategies.affected.DuplicateClassNameDifferentPackagesAsLocal",
                "org.arquillian.smart.testing.Files");
    }

    @Test
    public void should_resolve_annotations() {
        // given
        final JavaAssistClassParser javaAssistClassParser = new JavaAssistClassParser("");

        // when
        final JavaClass annotationClass =
            javaAssistClassParser.getClass(FullQualifiedNameAnnotationsClass.class.getName());

        // then
        assertThat(annotationClass.getImports())
            .containsExactlyInAnyOrder(
                "org.arquillian.smart.testing.strategies.affected.FullQualifiedNameAnnotationsClass",
                "org.arquillian.smart.testing.strategies.affected.FieldTestAnnotation",
                "org.arquillian.smart.testing.strategies.affected.ParameterTestAnnotation", "java.lang.Object",
                "org.arquillian.smart.testing.strategies.affected.TypeTestAnnotation",
                "org.arquillian.smart.testing.strategies.affected.DuplicateClassNameDifferentPackagesAsLocal",
                "java.lang.Integer");
    }
}
