package org.arquillian.smart.testing.mvn.ext;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YAML;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YML;

class ConfigLookup {

    private final File executionRootDir;
    private final File mavenProjectBaseDir;

    ConfigLookup(String executionRootDir) {
        this.executionRootDir = new File(executionRootDir);
        this.mavenProjectBaseDir = new File(System.getenv("MAVEN_PROJECTBASEDIR"));
    }

    File getFirstDirWithConfigOrProjectRootDir() {
        return getFirstDirWithConfigOrProjectRootDir(executionRootDir, mavenProjectBaseDir);
    }

    private File getFirstDirWithConfigOrProjectRootDir(File projectDir, File multiModuleProjectDirectory) {
        if (isSameFile(projectDir.toPath(), multiModuleProjectDirectory.toPath())) {
            return projectDir;
        }

        if (projectDir.isDirectory() && new File(projectDir, "pom.xml").exists() &&
            new File(projectDir, SMART_TESTING_YML).exists() || new File(projectDir, SMART_TESTING_YAML).exists()) {
            return projectDir;
        }

        return getFirstDirWithConfigOrProjectRootDir(projectDir.getParentFile(), multiModuleProjectDirectory);
    }

    private boolean isSameFile(Path path1, Path path2) {
        try {
            return Files.isSameFile(path1, path2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    boolean isConfigFromProjectRootDir() {
        final File configOrProjectRootDir = getFirstDirWithConfigOrProjectRootDir();
        return isSameFile(configOrProjectRootDir.toPath(), mavenProjectBaseDir.toPath());
    }

    boolean hasMoreThanOneConfigFile(String... fileNames){
        final long count;
        try {
            count = Files.walk(Paths.get(mavenProjectBaseDir.getAbsolutePath()))
                .parallel()
                .filter(p -> Arrays.asList(fileNames).contains(p.toFile().getName()))
                .count();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return count > 1;
    }
}
