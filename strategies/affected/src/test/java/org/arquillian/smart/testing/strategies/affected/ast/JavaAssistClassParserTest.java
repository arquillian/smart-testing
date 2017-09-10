package org.arquillian.smart.testing.strategies.affected.ast;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaAssistClassParserTest {

    @Test
    public void should_resolve_direct_imports() {
        // given
        final JavaAssistClassParser javaAssistClassParser = new JavaAssistClassParser();

        // when
        final JavaClass simpleImportsClass =
            javaAssistClassParser.getClass(SimpleImportsClass.class.getName());

        // then
        assertThat(simpleImportsClass.getImports())
            .containsExactlyInAnyOrder("java.util.Collections", "java.net.MalformedURLException", "java.net.URL",
                "org.arquillian.smart.testing.strategies.affected.ast.SimpleImportsClass", "java.lang.Object",
                "java.util.ArrayList");
    }

    @Test
    public void should_resolve_different_packages_with_same_class_name_fields() {
        // given
        final JavaAssistClassParser javaAssistClassParser = new JavaAssistClassParser();

        // when
        final JavaClass duplicatedClass =
            javaAssistClassParser.getClass(DuplicateClassNameDifferentPackagesAsField.class.getName());

        // then
        assertThat(duplicatedClass.getImports())
            .containsExactlyInAnyOrder("java.nio.file.Files",
                "org.arquillian.smart.testing.strategies.affected.ast.DuplicateClassNameDifferentPackagesAsField",
                "java.lang.Object", "org.assertj.core.util.Files", "org.arquillian.smart.testing.FilesCodec");
    }

    @Test
    public void should_resolve_different_packages_with_same_class_name_local_variables() {
        // given
        final JavaAssistClassParser javaAssistClassParser = new JavaAssistClassParser();

        // when
        final JavaClass duplicatedClass =
            javaAssistClassParser.getClass(DuplicateClassNameDifferentPackagesAsLocal.class.getName());

        // then
        assertThat(duplicatedClass.getImports())
            .containsExactlyInAnyOrder("java.nio.file.Paths", "java.nio.file.Files",
                "java.lang.Object", "java.io.IOException", "java.lang.String",
                "org.arquillian.smart.testing.strategies.affected.ast.DuplicateClassNameDifferentPackagesAsLocal",
                "org.arquillian.smart.testing.FilesCodec");
    }

    @Test
    public void should_resolve_annotations() {
        // given
        final JavaAssistClassParser javaAssistClassParser = new JavaAssistClassParser();

        // when
        final JavaClass annotationClass =
            javaAssistClassParser.getClass(FullQualifiedNameAnnotationsClass.class.getName());

        // then
        assertThat(annotationClass.getImports())
            .containsExactlyInAnyOrder(
                "org.arquillian.smart.testing.strategies.affected.ast.FullQualifiedNameAnnotationsClass",
                "org.arquillian.smart.testing.strategies.affected.ast.FieldTestAnnotation",
                "org.arquillian.smart.testing.strategies.affected.ast.ParameterTestAnnotation", "java.lang.Object",
                "org.arquillian.smart.testing.strategies.affected.ast.TypeTestAnnotation",
                "org.arquillian.smart.testing.strategies.affected.ast.DuplicateClassNameDifferentPackagesAsLocal",
                "java.lang.Integer");
    }
}
