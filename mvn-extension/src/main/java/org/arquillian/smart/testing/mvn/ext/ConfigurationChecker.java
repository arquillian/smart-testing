package org.arquillian.smart.testing.mvn.ext;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_CONFIG;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YAML;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YML;

class ConfigurationChecker {

    private final String projectDir;

    ConfigurationChecker(String projectDir) {
        this.projectDir = projectDir;
    }

    boolean hasModuleSpecificConfigurations() {
        return System.getProperty(SMART_TESTING_CONFIG) == null && hasMoreThanOneConfigFile();
    }

    private boolean hasMoreThanOneConfigFile(){
        final long count;
        try {
            count = Files.walk(Paths.get(projectDir))
                .parallel()
                .filter(p -> Arrays.asList(SMART_TESTING_YML, SMART_TESTING_YAML).contains(p.toFile().getName()))
                .count();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return count > 1;
    }

}
