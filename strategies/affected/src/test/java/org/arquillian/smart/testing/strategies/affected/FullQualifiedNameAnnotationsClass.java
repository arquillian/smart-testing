package org.arquillian.smart.testing.strategies.affected;

@TypeTestAnnotation
public class FullQualifiedNameAnnotationsClass {

    @org.arquillian.smart.testing.strategies.affected.FieldTestAnnotation
    private Integer age;

    public void firstCall(@org.arquillian.smart.testing.strategies.affected.ParameterTestAnnotation
        DuplicateClassNameDifferentPackagesAsLocal parameter) {

        parameter.firstCall();
    }
}
