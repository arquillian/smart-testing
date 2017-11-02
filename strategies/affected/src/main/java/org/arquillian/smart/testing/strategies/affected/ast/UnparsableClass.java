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
import java.util.Optional;

public class UnparsableClass implements JavaClass {
    private static final String[] NO_IMPORT = new String[0];

    private final String classname;

    UnparsableClass(String classname) {
        this.classname = classname;
    }

    @Override
    public File getClassFile() {
        return null;
    }

    @Override
    public <T> Optional<T> getAnnotationByType(Class<T> type) {
        return Optional.empty();
    }

    @Override
    public String[] getImports() {
        return NO_IMPORT;
    }

    @Override
    public String getName() {
        return classname;
    }

    @Override
    public String packageName() {
        return "";
    }

    @Override
    public String toString() {
        return "UnparsableClass{" + "classname='" + classname + '\'' + '}';
    }
}
