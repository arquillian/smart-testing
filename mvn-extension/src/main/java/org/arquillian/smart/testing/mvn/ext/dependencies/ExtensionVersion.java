package org.arquillian.smart.testing.mvn.ext.dependencies;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.arquillian.smart.testing.configuration.Configuration;

public class ExtensionVersion {

    private final static String VERSION_FILE = "/extension_version";
    private static final String NO_VERSION = "";

    private static Version version;

    public static Version version() {

        synchronized (ExtensionVersion.class) {
            if (version == null) {
                final String smartTestingVersion = System.getProperty(Configuration.SMART_TESTING_VERSION, NO_VERSION);
                if (NO_VERSION.equals(smartTestingVersion)) {
                    readFromFile();
                } else {
                    version = Version.from(smartTestingVersion);
                }
            }
        }

        return version;
    }

    private static void readFromFile() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ExtensionVersion.class.getResourceAsStream(VERSION_FILE)))){
            version = Version.from(reader.readLine().trim());
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read extension version", e);
        }
    }
}
