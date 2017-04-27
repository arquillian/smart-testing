package org.arquillian.smart.testing.strategies.affected.ast;

@TypeTestAnnotation
public class FullQualifiedNameAnnotationsClass {

    @FieldTestAnnotation
    private Integer age;

    public void firstCall(@ParameterTestAnnotation
        DuplicateClassNameDifferentPackagesAsLocal parameter) {

        parameter.firstCall();
    }
}
