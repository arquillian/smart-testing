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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.arquillian.smart.testing.Files;
import org.arquillian.smart.testing.strategies.affected.MissingClassException;
import org.arquillian.smart.testing.strategies.affected.UnparsableClass;

import static java.io.File.pathSeparator;

public class JavaAssistClassParser {
    private final String classpath;
    private ClassPool classPool;

    public JavaAssistClassParser(String classpath) {
        this.classpath = classpath;
    }

    public void clear() {
        // classPool = null;
    }

    private ClassPool getClassPool() {
        if (classPool == null) {
            // This is used primarily for getting Java core objects like String
            // and Integer,
            // so if we don't have the project's JDK classpath, it's probably
            // OK.
            classPool = new ClassPool(true);
            try {
                for (String pathElement : getPathElements()) {
                    classPool.appendClassPath(pathElement);
                }
            } catch (NotFoundException e) {
                classPool = null; // RISK Untested
                // Blank out the class pool so we try again next time
                throw new MissingClassException("Could not create class pool", e);
            }
        }
        return classPool;
    }

    private Iterable<String> getPathElements() {
        final String[] split = classpath.split(pathSeparator);
        List<String> entries = new ArrayList<>(Arrays.asList(split));
        ListIterator<String> iter = entries.listIterator();
        while (iter.hasNext()) {
            if (entryDoesNotExist(iter)) {
                iter.remove();
            }
        }
        return entries;
    }

    private boolean entryDoesNotExist(ListIterator<String> iter) {
        return !new File(iter.next()).exists();
    }

    private final static Map<String, JavaClass> CLASSES_BY_NAME = new HashMap<>();

    public JavaClass getClass(String className) {
        JavaClass clazz = CLASSES_BY_NAME.get(className);
        if (clazz == null) {
            CtClass ctClass = getCachedClass(className);

            if (unparsableClass(ctClass)) {
                clazz = new UnparsableClass(className);
            } else {
                try {
                    JavaAssistClass javaAssistClass = new JavaAssistClass(ctClass);
                    URL url = getClassPool().find(className);
                    if ((url != null) && url.getProtocol().equals("file")) {
                        javaAssistClass.setClassFile(new File(url.toURI()));
                    }
                    clazz = javaAssistClass;
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }

            CLASSES_BY_NAME.put(className, clazz);
        }

        return clazz;
    }

    private final static Map<String, CacheEntry> BY_PATH = new HashMap<>();

    public static class CacheEntry {
        final String sha1;
        final String classname;

        public CacheEntry(String sha1, String classname) {
            this.sha1 = sha1;
            this.classname = classname;
        }
    }

    public String classFileChanged(File file) throws IOException {
        String sha1 = Files.sha1(file);
        CacheEntry entry = BY_PATH.get(file.getAbsolutePath());
        if ((entry != null) && (entry.sha1.equals(sha1))) {
            return entry.classname;
        }

        final File clazzFile = getClassLocation(file);

        try (InputStream inputStream = new FileInputStream(clazzFile)) {

            CtClass ctClass = getClassPool().makeClass(inputStream);
            String classname = ctClass.getName();

            CLASSES_BY_NAME.remove(classname);
            BY_PATH.put(file.getAbsolutePath(), new CacheEntry(sha1, classname));

            return classname;
        }
    }

    // TODO dirty method to know where the .class is located instead of .java Topic for next cabal?
    // Good points of having this logic here is nobody needs to worry about this conversionin any module, bad point it is too deep
    // Also setting URL instead of a File implies that in TestClassDetector we need to convert ALL tests toa URL using Class.forName
    private File getClassLocation(File file) {
        final File clazzFile;
        if (file.getName().endsWith("Test.java")) {
            clazzFile =
                new File(
                    file.getAbsolutePath()
                        .replace("src/test/java", "target/test-classes")
                        .replace(".java", ".class")
                );
        } else {
            clazzFile =
                new File(
                    file.getAbsolutePath()
                        .replace("src/main/java", "target/classes")
                        .replace(".java", ".class")
                );
        }
        return clazzFile;
    }

    private boolean unparsableClass(CtClass cachedClass) {
        return cachedClass.getClassFile2() == null;
    }

    private CtClass getCachedClass(String className) {
        CtClass clazz = getClassPool().getOrNull(className);
        if (clazz == null) {
            throw new MissingClassException("Expected to find " + className);
        }
        return clazz;
    }
}
