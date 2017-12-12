package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.strategies.affected.ast.JavaClassBuilder;

class ElementAdapter {

    private final TestVerifier testVerifier;
    private final JavaClassBuilder builder;

    ElementAdapter(TestVerifier testVerifier, JavaClassBuilder javaClassBuilder) {
        this.testVerifier = testVerifier;
        this.builder = javaClassBuilder;
    }

    Optional<Element> tranform(Change change) {

        final Path element = change.getLocation();

        if (testVerifier.isCore(element)) {

            final File classLocation = JavaToClassLocation.transform(element.toFile(), testVerifier);
            final String className = this.builder.getClassName(classLocation);
            return Optional.of(new JavaElement(this.builder.getClassDescription(className)));
        } else {
            if (!testVerifier.isJavaFile(element)) {
                return Optional.of(
                    new FileElement(element));
            } else {
                return Optional.empty();
            }
        }
    }
}
