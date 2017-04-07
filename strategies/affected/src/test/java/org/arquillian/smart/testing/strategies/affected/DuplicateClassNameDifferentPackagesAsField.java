package org.arquillian.smart.testing.strategies.affected;

import org.arquillian.smart.testing.Files;

public class DuplicateClassNameDifferentPackagesAsField {

    private Files files;
    private org.assertj.core.util.Files files2;
    private java.nio.file.Files files3;

    public void callFiles() {
        files.hashCode();
    }

    public void callFiles2() {
        files2.hashCode();
    }

    public void callFiles3() {
        files3.hashCode();
    }
}
