package org.arquillian.smart.testing.strategies.affected.ast;

import java.util.ArrayList;
import java.util.Collection;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.CharMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.DoubleMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.LongMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.MemberValueVisitor;
import javassist.bytecode.annotation.ShortMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

class ClassExtractorAnnotationMemberValue implements MemberValueVisitor {

    private Collection<String> imports = new ArrayList<>();

    @Override
    public void visitAnnotationMemberValue(AnnotationMemberValue node) {
    }

    @Override
    public void visitArrayMemberValue(ArrayMemberValue node) {
        final MemberValue type = node.getType();
        if (type != null && type.toString().endsWith(".class")) {
            final String clazz = type.toString();
            imports.add(extractClass(clazz));
        }
    }

    @Override
    public void visitBooleanMemberValue(BooleanMemberValue node) {
    }

    @Override
    public void visitByteMemberValue(ByteMemberValue node) {
    }

    @Override
    public void visitCharMemberValue(CharMemberValue node) {
    }

    @Override
    public void visitDoubleMemberValue(DoubleMemberValue node) {
    }

    @Override
    public void visitEnumMemberValue(EnumMemberValue node) {
        if (node.getType() != null) {
            imports.add(node.getType());
        }
    }

    @Override
    public void visitFloatMemberValue(FloatMemberValue node) {
    }

    @Override
    public void visitIntegerMemberValue(IntegerMemberValue node) {
    }

    @Override
    public void visitLongMemberValue(LongMemberValue node) {
    }

    @Override
    public void visitShortMemberValue(ShortMemberValue node) {
    }

    @Override
    public void visitStringMemberValue(StringMemberValue node) {
    }

    @Override
    public void visitClassMemberValue(ClassMemberValue node) {
        imports.add(node.getValue());
    }

    private String extractClass(String clazz) {
        return clazz.substring(0, clazz.lastIndexOf(".class"));
    }


    Collection<String> getImports() {
        return imports;
    }
}
