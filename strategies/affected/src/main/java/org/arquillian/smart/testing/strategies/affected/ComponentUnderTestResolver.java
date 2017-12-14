package org.arquillian.smart.testing.strategies.affected;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.strategies.affected.ast.JavaClass;

class ComponentUnderTestResolver {

    private static final Logger logger = Log.getLogger();

    List<String> resolve(JavaClass testJavaClass) {
        final List<String> manualDependencyClasses = new ArrayList<>();
        final ComponentUnderTest[] allTestsAnnotation = findComponentsUnderTests(testJavaClass);

        for (ComponentUnderTest tests : allTestsAnnotation) {
            List<String> packages = getPackages(testJavaClass.packageName(), tests);
            for (String pkg : packages) {
                final String trimmedPackage = pkg.trim();
                manualDependencyClasses.addAll(scanClassesFromPackage(trimmedPackage));
            }

            final Class[] classes = tests.classes();
            for (Class clazz : classes) {
                manualDependencyClasses.add(clazz.getName());
            }
        }

        return manualDependencyClasses;
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

}
