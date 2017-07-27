package org.arquillian.smart.testing.mvn.ext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ExtensionVersion {

    private final static String VERSION_FILE = "/extension_version";
    public static final String NO_VERSION = "";

    private static String version;

    public static String version() {

        synchronized (ExtensionVersion.class) {
            if (version == null) {
                final String smartTestingVersion = System.getProperty("smart.testing.version", NO_VERSION);
                if (NO_VERSION.equals(smartTestingVersion)) {
                    readFromFile();
                } else {
                    version = smartTestingVersion;
                }
            }
        }

        return version;
    }

    private static void readFromFile() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ExtensionVersion.class.getResourceAsStream(VERSION_FILE)))){
            version = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read extension version", e);
        }
    }
}
