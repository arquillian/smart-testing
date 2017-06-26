package org.arquillian.smart.testing.strategies.affected;

import java.io.File;

public class JavaToClassLocation {

    private JavaToClassLocation() {
    }

    public static File transform(File javaLocation) {

        // TODO dirty method to know where the .class is located instead of .java Topic for next cabal?
        // Also setting URL instead of a File implies that in TestClassDetector we need to convert ALL tests to URL using Class.forName
        final File clazzFile;
        if (javaLocation.getName().endsWith("Test.java")) {
            clazzFile =
                new File(
                    javaLocation.getAbsolutePath()
                        .replace("src/test/java", "target/test-classes")
                        .replace(".java", ".class")
                );
        } else {
            clazzFile =
                new File(
                    javaLocation.getAbsolutePath()
                        .replace("src/main/java", "target/classes")
                        .replace(".java", ".class")
                );
        }

        return clazzFile;
    }
}
