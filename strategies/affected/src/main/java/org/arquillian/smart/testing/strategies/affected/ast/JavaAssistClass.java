/*
 * Infinitest, a Continuous Test Runner.
 *
 * Copyright (C) 2010-2013
 * "Ben Rady" <benrady@gmail.com>,
 * "Rod Coffin" <rfciii@gmail.com>,
 * "Ryan Breidenbach" <ryan.breidenbach@gmail.com>
 * "David Gageot" <david@gageot.net>, et al.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.arquillian.smart.testing.strategies.affected.ast;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

import static javassist.bytecode.AnnotationsAttribute.invisibleTag;
import static javassist.bytecode.AnnotationsAttribute.visibleTag;

/**
 * Be careful: instances of this class are kept in a cache
 * so we should keep its footprint minimal.
 */
public class JavaAssistClass extends AbstractJavaClass {
    private final String[] imports;
    private final String className;
    private File classFile;
    private final CtClass classReference;

    JavaAssistClass(CtClass classReference) {
        this.imports = findImports(classReference);
        this.className = classReference.getName();
        this.classReference = classReference;
    }

    @Override
    public String[] getImports() {
        return imports;
    }

    private String[] findImports(CtClass ctClass) {
        Set<String> imports = new HashSet<>();
        addDependenciesFromConstantPool(ctClass, imports);
        addFieldDependencies(ctClass, imports);
        addClassAnnotationDependencies(ctClass, imports);
        addFieldAnnotationDependencies(ctClass, imports);
        addMethodAnnotationDependencies(ctClass, imports);

        String[] array = new String[imports.size()];

        int index = 0;
        for (String anImport : imports) {
            array[index++] = anImport.intern(); // Use less memory
        }
        return array;
    }

    private void addFieldAnnotationDependencies(CtClass ctClass, Collection<String> imports) {
        for (CtField field : ctClass.getDeclaredFields()) {
            List<?> attributes = field.getFieldInfo2().getAttributes();
            addAnnotationsForAttributes(imports, attributes);
        }
    }

    private void addFieldDependencies(CtClass ctClass, Collection<String> imports) {
        for (CtField field : ctClass.getDeclaredFields()) {
            imports.add(DescriptorParser.parseClassNameFromConstantPoolDescriptor(field.getFieldInfo2().getDescriptor()));
        }
    }

    private void addMethodAnnotationDependencies(CtClass ctClass, Collection<String> imports) {
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            MethodInfo methodInfo = ctMethod.getMethodInfo2();
            List<?> attributes = methodInfo.getAttributes();
            addAnnotationsForAttributes(imports, attributes);
            addParameterAnnotationsFor(imports, methodInfo, ParameterAnnotationsAttribute.visibleTag);
            addParameterAnnotationsFor(imports, methodInfo, ParameterAnnotationsAttribute.invisibleTag);
        }
    }

    private void addAnnotationsForAttributes(Collection<String> imports, List<?> attributes) {
        for (Object each : attributes) {
            if (each instanceof AnnotationsAttribute) {
                addAnnotations(imports, (AnnotationsAttribute) each);
            }
        }
    }

    private void addParameterAnnotationsFor(Collection<String> imports, MethodInfo methodInfo, String tag) {
        AttributeInfo attribute = methodInfo.getAttribute(tag);
        ParameterAnnotationsAttribute annotationAttribute = (ParameterAnnotationsAttribute) attribute;
        if (annotationAttribute != null) {
            Annotation[][] parameters = annotationAttribute.getAnnotations();
            for (Annotation[] annotations : parameters) {
                for (Annotation annotation : annotations) {
                    imports.add(annotation.getTypeName());
                }
            }
        }
    }

    private void addClassAnnotationDependencies(CtClass classReference, Collection<String> imports) {
        addClassAnnotationsOfTagType(classReference, imports, visibleTag);
        addClassAnnotationsOfTagType(classReference, imports, invisibleTag);
    }

    private void addClassAnnotationsOfTagType(CtClass classRef, Collection<String> imports, String tag) {
        addAnnotations(imports, getAnnotationsOfType(tag, classRef));
    }

    private AnnotationsAttribute getAnnotationsOfType(String tag, CtClass classRef) {
        return (AnnotationsAttribute) classRef.getClassFile2().getAttribute(tag);
    }

    private void addAnnotations(Collection<String> imports, AnnotationsAttribute annotations) {
        if (annotations != null) {
            for (Annotation each : annotations.getAnnotations()) {
                imports.add(each.getTypeName());
            }
        }
    }

    private void addDependenciesFromConstantPool(CtClass ctClass, Collection<String> imports) {
        ConstPool constPool = ctClass.getClassFile2().getConstPool();
        Set<?> classNames = constPool.getClassNames();
        for (Object each : classNames) {
            imports.add(pathToClassName(each.toString()));
        }
    }

    private String pathToClassName(String classPath) {
        return classPath.replace('/', '.');
    }

    @Override
    public String getName() {
        return className;
    }

    @Override
    public String packageName() {
        return classReference.getPackageName();
    }

    @Override
    public String toString() {
        return getName();
    }

    public void setClassFile(File classFile) {
        this.classFile = classFile;
    }

    @Override
    public File getClassFile() {
        return classFile;
    }

    @Override
    public <T> Optional<T> getAnnotationByType(Class<T> type) {
        try {
            Object annotation = this.classReference.getAnnotation(type);

            if (annotation == null) {
                CtClass superclass = this.classReference.getSuperclass();

                // We need a string representation because if not JavaAssist throws an exception
                String superClassClass = superclass.getName();
                while (annotation == null && !Object.class.getName().equals(superClassClass)) {
                    annotation = superclass.getAnnotation(type);
                    superclass = superclass.getSuperclass();
                    superClassClass = superclass.getName();
                }

            }

            return Optional.ofNullable((T) annotation);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (NotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
