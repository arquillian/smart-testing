package org.arquillian.smart.testing.strategies.affected.ast;

import java.io.IOException;
import java.nio.file.Paths;
import org.arquillian.smart.testing.FilesCodec;

public class DuplicateClassNameDifferentPackagesAsLocal {

    public void firstCall() {
        FilesCodec.bytesToHex(new byte[0]);
    }

    public void secondCall() throws IOException {
        java.nio.file.Files.size(Paths.get("."));
    }

}
