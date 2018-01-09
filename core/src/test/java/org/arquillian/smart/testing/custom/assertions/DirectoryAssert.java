package org.arquillian.smart.testing.custom.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.FileAssert;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

public class DirectoryAssert extends AbstractAssert<DirectoryAssert, Path> {

    DirectoryAssert(Path actual) {
        super(actual, DirectoryAssert.class);
    }

    public static DirectoryAssert assertThatDirectory(Path path) {
        return new DirectoryAssert(path);
    }

    public static DirectoryAssert assertThatDirectory(File file) {
        return new DirectoryAssert(file.toPath());
    }

    public DirectoryAssert hasSameContentAs(Path path) {
        FileAssert fileAssert = new FileAssert(actual.toFile());
        fileAssert.exists().isDirectory();

        Arrays
            .stream(path.toFile().listFiles())
            .map(File::toPath)
            .forEach(expectedFile -> {
                Path actualFile = actual.resolve(expectedFile.getFileName());
                if (expectedFile.toFile().isDirectory()) {
                    assertThatDirectory(actualFile).hasSameContentAs(expectedFile);
                } else {
                    Assertions.assertThat(actualFile).exists().isRegularFile().hasSameContentAs(expectedFile);
                }
            });

        return this;
    }

}
