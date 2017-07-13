package org.arquillian.smart.testing;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;

class ClassNameExtractor {

    String extractFullyQualifiedName(File sourceFile) throws FileNotFoundException {
        // FIXME takes ages
        final CompilationUnit compilationUnit = JavaParser.parse(sourceFile);
        final Optional<ClassOrInterfaceDeclaration> newClass =
            compilationUnit.getClassByName(sourceFile.getName().replaceAll(".java", ""));
        final PackageDeclaration packageDeclaration =
            compilationUnit.getPackageDeclaration().orElseGet(PackageDeclaration::new);
        final String fullyQualifiedName = packageDeclaration.getNameAsString() + "." + newClass.get().getNameAsString();
        return fullyQualifiedName.replace("empty.", "");
    }
}
