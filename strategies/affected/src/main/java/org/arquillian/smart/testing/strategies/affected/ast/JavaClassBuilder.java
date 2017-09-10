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
import java.io.IOException;
import javassist.NotFoundException;
import org.arquillian.smart.testing.strategies.affected.MissingClassException;

/**
 * @author Ben Rady
 */
public class JavaClassBuilder {
    private final JavaAssistClassParser parser;

    public JavaClassBuilder() {
        this(new JavaAssistClassParser());
    }

    public JavaClassBuilder(JavaAssistClassParser parser) {
        this.parser = parser;
    }

    public JavaClass getClassDescription(String classname) {
        try {
            return parser.getClass(classname);
        } catch (RuntimeException e) {
            // Can occur when a cached class disappears from the file system
            rethrowIfSerious(e);
            return explicitlyCreateJavaClass(classname);
        } catch (MissingClassException e) {
            return explicitlyCreateJavaClass(classname);
        }
    }

    private JavaClass explicitlyCreateJavaClass(String classname) {
        try {
            parser.makeClass(classname);
            return parser.getClass(classname);
        } catch(Exception e) {
            return new UnparsableClass(classname);
        }
    }

    /**
     * Returns class name from given .class file.
     */
    public String getClassName(File file) {
        try {
            return parser.getClassName(file);
        } catch (RuntimeException e) {
            // If the class goes missing after we read it in but before we
            // process it,
            // we might get an exception that looks like this
            rethrowIfSerious(e);
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private void rethrowIfSerious(RuntimeException e) {
        if (!(e.getCause() instanceof NotFoundException)) {
            throw e;
        }
    }
}
