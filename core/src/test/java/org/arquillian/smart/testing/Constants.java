package org.arquillian.smart.testing;

import java.io.File;
import java.nio.file.Paths;

public final class Constants {

    private Constants() {
    }

    public static final File CURRENT_DIR = Paths.get("").toFile();
}
