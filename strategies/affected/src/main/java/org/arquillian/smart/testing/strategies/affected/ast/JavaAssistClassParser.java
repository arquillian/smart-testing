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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.arquillian.smart.testing.FilesCodec;
import org.arquillian.smart.testing.strategies.affected.MissingClassException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class JavaAssistClassParser {
    private ClassPool classPool;

    public JavaAssistClassParser() {
    }

    private ClassPool getClassPool() {
        if (classPool == null) {
            // This is used primarily for getting Java core objects like String
            // and Integer,
            // so if we don't have the project's JDK classpath, it's probably
            // OK.
            classPool = new ClassPool(true);
            try {
                Arrays.stream(getLoadedClasses()).map(URL::toExternalForm).forEach(
                    s -> {
                        try {
                            final String jarLocation = s.replace("file:", "");
                            classPool.appendClassPath(URLDecoder.decode(jarLocation, "UTF-8")); // removes file prefix, as JavaAssist doesn't like it
                        } catch (NotFoundException e) {
                            throw new RuntimeException("Failed configuring JavaAssist ClassPool" +
                                " while loading resources from Context Class Loader", e);
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                );

            } catch (RuntimeException e) {
                classPool = null; // RISK Untested
                // Blank out the class pool so we try again next time
                throw new MissingClassException("Could not create class pool", e);
            }
        }
        return classPool;
    }

    private URL[] getLoadedClasses()
    {
        return ((URLClassLoader) (Thread.currentThread().getContextClassLoader())).getURLs();
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

        CacheEntry(String sha1, String classname) {
            this.sha1 = sha1;
            this.classname = classname;
        }
    }

    /**
     * Returns the classname of given .class file.
     */
    String getClassName(File file) throws IOException {
        String sha1 = FilesCodec.sha1(file);
        CacheEntry entry = BY_PATH.get(file.getAbsolutePath());
        if ((entry != null) && (entry.sha1.equals(sha1))) {
            return entry.classname;
        }

        try (InputStream inputStream = new FileInputStream(file)) {
            CtClass ctClass = makeClass(inputStream);
            String classname = ctClass.getName();

            CLASSES_BY_NAME.remove(classname);
            BY_PATH.put(file.getAbsolutePath(), new CacheEntry(sha1, classname));

            return classname;
        }
    }

    CtClass makeClass(String className) {
        return getClassPool().makeClass(className);
    }

    CtClass makeClass(InputStream className) throws IOException {
        return getClassPool().makeClass(className);
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
