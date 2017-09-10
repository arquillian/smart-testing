package org.arquillian.smart.testing;

import java.io.File;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassNameExtractorTest {

    @Test
    public void should_extract_fully_qualified_name_from_source_file_with_defined_package() throws Exception {
        // given
        final ClassNameExtractor classNameExtractor = new ClassNameExtractor();

        // when
        final File dummyClass = load("DummyClassWithPackageName.java");
        final String fullyQualifiedName = classNameExtractor.extractFullyQualifiedName(dummyClass);

        // then
        assertThat(fullyQualifiedName).isEqualTo("dummy.cls.DummyClassWithPackageName");
    }

    @Test
    public void should_extract_fully_qualified_name_from_source_file_with_single_line_comment() throws Exception {
        // given
        final ClassNameExtractor classNameExtractor = new ClassNameExtractor();

        // when
        final File dummyClass = load("DummyClassWithSingleLineComment.java");
        final String fullyQualifiedName = classNameExtractor.extractFullyQualifiedName(dummyClass);

        // then
        assertThat(fullyQualifiedName).isEqualTo("dummy.cls.DummyClassWithSingleLineComment");
    }

    @Test
    @Ignore("FIXME: improve the pattern to handle this case")
    public void should_extract_fully_qualified_name_from_source_file_with_defined_package_ignoring_comments() throws Exception {
        // given
        final ClassNameExtractor classNameExtractor = new ClassNameExtractor();

        // when
        final File dummyClass = load("HostileDummyClassWithPackageName.java");
        final String fullyQualifiedName = classNameExtractor.extractFullyQualifiedName(dummyClass);

        // then
        assertThat(fullyQualifiedName).isEqualTo("dummy.correct.cls.HostileDummyClassWithPackageName");
    }

    @Test
    public void should_extract_fully_qualified_name_from_source_file_with_default_package() throws Exception {
        // given
        final ClassNameExtractor classNameExtractor = new ClassNameExtractor();

        // when
        final File dummyClass = load("DummyClassWithDefaultPackageName.java");
        final String fullyQualifiedName = classNameExtractor.extractFullyQualifiedName(dummyClass);

        // then
        assertThat(fullyQualifiedName).isEqualTo("DummyClassWithDefaultPackageName");
    }

    private File load(String fileName) {
        return new File(Thread.currentThread().getContextClassLoader().getResource(
            fileName).getFile());
    }
}
