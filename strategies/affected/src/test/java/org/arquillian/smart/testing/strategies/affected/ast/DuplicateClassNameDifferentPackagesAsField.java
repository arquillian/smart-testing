package org.arquillian.smart.testing.strategies.affected.ast;

import org.arquillian.smart.testing.FilesCodec;

public class DuplicateClassNameDifferentPackagesAsField {

    private FilesCodec filesCodec;
    private org.assertj.core.util.Files files2;
    private java.nio.file.Files files3;

    public void callFiles() {
        filesCodec.hashCode();
    }

    public void callFiles2() {
        files2.hashCode();
    }

    public void callFiles3() {
        files3.hashCode();
    }
}
