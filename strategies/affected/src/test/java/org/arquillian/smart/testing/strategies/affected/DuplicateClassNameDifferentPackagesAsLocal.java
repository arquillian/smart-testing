package org.arquillian.smart.testing.strategies.affected;

import java.io.IOException;
import java.nio.file.Paths;
import org.arquillian.smart.testing.Files;

public class DuplicateClassNameDifferentPackagesAsLocal {

    public void firstCall() {
        Files.bytesToHex(new byte[0]);
    }

    public void secondCall() throws IOException {
        java.nio.file.Files.size(Paths.get("."));
    }

}
