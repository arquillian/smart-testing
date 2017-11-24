/*
 * Derived from Infinitest, a Continuous Test Runner.
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

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.api.TestVerifier;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.strategies.affected.ast.JavaClass;
import org.arquillian.smart.testing.strategies.affected.ast.JavaClassBuilder;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import static org.arquillian.smart.testing.strategies.affected.AffectedTestsDetector.AFFECTED;
import static org.jgrapht.Graphs.predecessorListOf;

public class ClassDependenciesGraph {

    private static final Logger logger = Log.getLogger();
    private static final Filter coreJava = new Filter(Collections.singletonList(""), Collections.singletonList("java.*"));

    private final JavaClassBuilder builder;
    private final DirectedGraph<Element, DefaultEdge> graph;
    private final Filter filter;
    private final TestVerifier testVerifier;
    private final boolean enableTransitivity;
    private final Path projectDir;

    ClassDependenciesGraph(TestVerifier testVerifier, Configuration configuration, File projectDir) {
        this.builder = new JavaClassBuilder();
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        final AffectedConfiguration affectedConfiguration =
            (AffectedConfiguration) configuration.getStrategyConfiguration(AFFECTED);
        this.filter = new Filter(affectedConfiguration.getInclusions(), affectedConfiguration.getExclusions());
        this.testVerifier = testVerifier;
        this.enableTransitivity = affectedConfiguration.isTransitivity();
        this.projectDir = projectDir.toPath();
    }

    void buildTestDependencyGraph(Collection<File> modifiedJavaFiles) {
        // First update class index
        List<String> testClassesNames = new ArrayList<>();
        for (File testJavaFile : modifiedJavaFiles) {
            String changedTestClassClassname =
                builder.getClassName(JavaToClassLocation.transform(testJavaFile, testVerifier));
            if (changedTestClassClassname != null) {
                testClassesNames.add(changedTestClassClassname);
            }
        }

        // Then find dependencies
        for (String changedTestClassNames : testClassesNames) {
            JavaClass testJavaClass = builder.getClassDescription(changedTestClassNames);
            if (testJavaClass != null) {
                final String[] imports = testJavaClass.getImports();

                final List<String> manualProductionClasses = calculateManuallyAddedDependencies(testJavaClass);
                manualProductionClasses.addAll(Arrays.asList(imports));

                final List<Path> files = calculateWatchedFiles(testJavaClass);

                addToIndex(new JavaElement(testJavaClass), manualProductionClasses, files);
            }
        }
    }

    private List<Path> calculateWatchedFiles(JavaClass testJavaClass) {
        final List<Path> files = new ArrayList<>();
        final WatchFile[] allTestsAnnotation = findWatchFiles(testJavaClass);

        for (WatchFile file : allTestsAnnotation) {
            files.add(projectDir.resolve(file.value()).normalize());
        }

        return files;
    }

    private List<String> calculateManuallyAddedDependencies(JavaClass testJavaClass) {
        final List<String> manualDependencyClasses = new ArrayList<>();
        final ComponentUnderTest[] allTestsAnnotation = findComponentsUnderTests(testJavaClass);

        for (ComponentUnderTest tests : allTestsAnnotation) {
            List<String> packages = getPackages(testJavaClass.packageName(), tests);
            for (String pkg : packages) {
                final String trimmedPackage = pkg.trim();
                manualDependencyClasses.addAll(scanClassesFromPackage(trimmedPackage));
            }
        }

        return manualDependencyClasses;
    }

    private WatchFile[] findWatchFiles(JavaClass testJavaClass) {

        final Optional<WatchFiles> testsListOptional = testJavaClass.getAnnotationByType(WatchFiles.class);

        WatchFile[] tests = testsListOptional
            .map(WatchFiles::value)
            .orElseGet(() -> testJavaClass.getAnnotationByType(WatchFile.class)
                .map(annotation -> new WatchFile[] {annotation})
                .orElse(new WatchFile[0]));

        return tests;
    }

    private ComponentUnderTest[] findComponentsUnderTests(JavaClass testJavaClass) {

        final Optional<ComponentsUnderTest> testsListOptional =
            testJavaClass.getAnnotationByType(ComponentsUnderTest.class);

        ComponentUnderTest[] tests = testsListOptional
            .map(ComponentsUnderTest::value)
            .orElseGet(() -> testJavaClass.getAnnotationByType(ComponentUnderTest.class)
                .map(annotation -> new ComponentUnderTest[] {annotation})
                .orElse(new ComponentUnderTest[0]));

        return tests;
    }

    private List<String> scanClassesFromPackage(String trimmedPackage) {
        final List<String> manualDependencyClasses = new ArrayList<>();
        if (trimmedPackage.endsWith(".*")) {
            String realPackage = trimmedPackage.substring(0, trimmedPackage.indexOf(".*"));
            final List<String> classesOfPackage =
                new FastClasspathScanner(realPackage).scan()
                    .getNamesOfAllClasses();

            manualDependencyClasses.addAll(
                classesOfPackage);
        } else {
            final List<String> classesOfPackage =
                new FastClasspathScanner(trimmedPackage).disableRecursiveScanning().scan()
                    .getNamesOfAllClasses();
            manualDependencyClasses.addAll(
                classesOfPackage);
        }

        if (manualDependencyClasses.isEmpty()) {
            logger.warn(
                "You set %s package as reference classes to run tests, but no classes found. Maybe a package refactor?",
                trimmedPackage);
        }

        return manualDependencyClasses;
    }

    private List<String> getPackages(String testPackage, ComponentUnderTest tests) {
        List<String> packages = new ArrayList<>();
        if (tests.classes().length == 0 && tests.packages().length == 0 && tests.packagesOf().length == 0) {
            packages.add(testPackage + ".*");
        } else {
            packages.addAll(Arrays.asList(tests.packages()));

            packages.addAll(Arrays.stream(tests.packagesOf())
                .map(clazz -> clazz.getPackage().getName())
                .collect(Collectors.toList()));
        }
        return packages;
    }

    private void addToIndex(JavaElement javaElement, List<String> imports, List<Path> files) {
        addToGraph(javaElement);
        updateJavaElementWithImportReferences(javaElement, imports);
        updateJavaElementWithFiles(javaElement, files);
    }

    private void updateJavaElementWithFiles(JavaElement javaElement, List<Path> files) {
        for (Path file : files) {
            final FileElement fileElement = new FileElement(file);

            if (!graph.containsVertex(fileElement)) {
                graph.addVertex(fileElement);
            }

            if (!graph.containsEdge(javaElement, fileElement)) {
                graph.addEdge(javaElement, fileElement);
            }
        }
    }

    private void addToGraph(JavaElement newClass) {
        if (!graph.addVertex(newClass)) {
            replaceVertex(newClass);
        }
    }

    private void replaceVertex(JavaElement newClass) {
        List<Element> incomingEdges = getParents(newClass);

        graph.removeVertex(newClass);
        graph.addVertex(newClass);
        for (Element each : incomingEdges) {
            graph.addEdge(each, newClass);
        }
    }

    private void updateJavaElementWithImportReferences(JavaElement javaElementParentClass, List<String> imports) {

        for (String importz : imports) {

            if (addImport(javaElementParentClass, importz)
                && filter.shouldBeIncluded(importz)
                && this.enableTransitivity) {
                JavaClass javaClass = builder.getClassDescription(importz);
                if (javaClass != null) {
                    updateJavaElementWithImportReferences(javaElementParentClass, Arrays.asList(javaClass.getImports()));
                }
            }
        }
    }

    private boolean addImport(JavaElement javaElementParentClass, String importz) {

        if (coreJava.shouldBeIncluded(importz)) {

            JavaElement importClass = new JavaElement(importz);
            if (!importClass.equals(javaElementParentClass)) {
                if (!graph.containsVertex(importClass)) {
                    graph.addVertex(importClass);
                }

                // This condition is required because we are flattening imports in graph
                if (!graph.containsEdge(javaElementParentClass, importClass)) {
                    graph.addEdge(javaElementParentClass, importClass);
                    return true;
                }
            }
        }

        return false;
    }

    Set<String> findTestsDependingOn(Collection<Change> files) {

        return files.stream()
            .map(Change::getLocation)
            .map(this::transformToElement)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(graph::containsVertex)
            .map(this::getParents)
            .flatMap(Collection::stream)
            .map(e -> (JavaElement) e)
            .map(JavaElement::getClassName)
            .collect(Collectors.toSet());
    }

    private Optional<Element> transformToElement(Path element) {

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

    private List<Element> getParents(Element childClass) {
        return predecessorListOf(graph, childClass);

    }

    @Override
    public String toString() {
        final Set<DefaultEdge> defaultEdges = graph.edgeSet();
        StringBuilder s = new StringBuilder(System.lineSeparator());

        defaultEdges.forEach(de -> s.append(de.toString()).append(System.lineSeparator()));

        return s.toString();
    }
}
