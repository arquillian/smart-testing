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
package org.arquillian.smart.testing.strategies.affected;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.strategies.affected.ast.JavaClass;
import org.arquillian.smart.testing.strategies.affected.ast.JavaClassBuilder;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import static org.jgrapht.Graphs.predecessorListOf;

public class ClassFileIndex {

    private static final int MAX_DEPTH = AffectedRunnerProperties.getSmartTestingDepthLevel();

    private Logger logger = Logger.getLogger(ClassFileIndex.class);

    private final JavaClassBuilder builder;
    private DirectedGraph<JavaElement, DefaultEdge> graph;
    private List<String> globPatterns;

    public ClassFileIndex(ClasspathProvider classpath) {
        this(new JavaClassBuilder(classpath));
    }

    public ClassFileIndex(ClasspathProvider classpath, List<String> globPatterns) {
        this(new JavaClassBuilder(classpath));
        this.globPatterns = globPatterns;
    }

    ClassFileIndex(JavaClassBuilder classBuilder) {
        builder = classBuilder;
        graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    }

    public void buildTestDependencyGraph(Collection<File> testJavaFiles) {
        // First update class index
        List<String> testClassesNames = new ArrayList<>();
        for (File testJavaFile : testJavaFiles) {
            String changedTestClassClassname = builder.classFileChanged(JavaToClassLocation.transform(testJavaFile, globPatterns));
            if (changedTestClassClassname != null) {
                testClassesNames.add(changedTestClassClassname);
            }
        }

        // Then find dependencies
        Set<JavaClass> changedTestClasses = new HashSet<>();
        for (String changedTestClassNames : testClassesNames) {
            JavaClass javaClass = builder.getClass(changedTestClassNames);
            if (javaClass != null) {
                addToIndex(new JavaElement(javaClass), javaClass.getImports());
                changedTestClasses.add(javaClass);
            }
        }
        builder.clear();
    }

    private void addToIndex(JavaElement javaElement, String[] imports) {
        addToGraph(javaElement);
        updateJavaElementWithImportReferences(javaElement, imports, 0);
    }

    private void addToGraph(JavaElement newClass) {
        if (!graph.addVertex(newClass)) {
            replaceVertex(newClass);
        }
    }

    private void replaceVertex(JavaElement newClass) {
        List<JavaElement> incomingEdges = getParents(newClass);

        graph.removeVertex(newClass);
        graph.addVertex(newClass);
        for (JavaElement each : incomingEdges) {
            graph.addEdge(each, newClass);
        }
    }

    private void updateJavaElementWithImportReferences(JavaElement javaElementParentClass,
        String[] imports, int currentDepth) {

        if (currentDepth >= MAX_DEPTH) return;

        for (String importz : imports) {

            if (addImport(javaElementParentClass, importz)) {

                JavaClass javaClass = builder.getClass(importz);
                if (javaClass != null) {
                    updateJavaElementWithImportReferences(javaElementParentClass, javaClass.getImports(),
                        currentDepth + 1);
                }
            }
        }
    }

    private boolean addImport(JavaElement javaElementParentClass, String importz) {
        // Avoid adding JDK classes (java, javax)
        // Probably we need to make this configurable or provide some defaults like TomEE does: https://github.com/apache/tomee/blob/master/container/openejb-core/src/main/resources/default.exclusions
        // To avoid scanning for example org.springframework.*, org.apache.*, ...  and provide a flag to avoid it as well
        // Or another option would be do something inclusion which by default could be the first two parts of test package (i.e or.mycompany)
        if (!importz.startsWith("java")) {

            JavaElement importClass = new JavaElement(importz);
            if (!importClass.equals(javaElementParentClass)) {
                if (!graph.containsVertex(importClass)) {
                    graph.addVertex(importClass);
                }

                // This condition is required because we are flattering imports in graph
                // This operation is fast since it is a containsKey call in a Map instance
                if (!graph.containsEdge(javaElementParentClass, importClass)) {
                    graph.addEdge(javaElementParentClass, importClass);
                    return true;
                }
                graph.addEdge(javaElementParentClass, importClass);
            }
        }

        return false;
    }


    public Set<String> findTestsDependingOn(Set<File> classes) {
        return classes.stream()
            .map(javaClass -> JavaToClassLocation.transform(javaClass, globPatterns))
            .map(this.builder::classFileChanged)
            .map(this.builder::getClass)
            .map(JavaElement::new)
            .filter(graph::containsVertex)
            .map(this::getParents)
            .flatMap(l -> l.stream())
            .map(JavaElement::getClassName)
            .collect(Collectors.toSet());
    }

    private List<JavaElement> getParents(JavaElement childClass) {
        return predecessorListOf(graph, childClass);
    }

    public void clear() {
        graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    }

    public boolean isIndexed(Class<Object> clazz) {
        return getIndexedClasses().contains(clazz.getName());
    }

    public Set<String> getIndexedClasses() {
        Set<String> classes = new HashSet<>();
        Set<JavaElement> vertexSet = graph.vertexSet();
        for (JavaElement each : vertexSet) {
            classes.add(each.getClassName());
        }
        return classes;
    }

    @Override
    public String toString() {
        final Set<DefaultEdge> defaultEdges = graph.edgeSet();
        StringBuilder s = new StringBuilder(System.lineSeparator());

        defaultEdges.stream().forEach(de -> s.append(de.toString()).append(System.lineSeparator()));

        return s.toString();
    }

}
