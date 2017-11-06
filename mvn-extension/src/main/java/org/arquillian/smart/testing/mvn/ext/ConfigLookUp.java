package org.arquillian.smart.testing.mvn.ext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;

import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YAML;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YML;

class ConfigLookUp {

    private final MavenExecutionRequest mavenExecutionRequest;
    private final File executionDir;

    ConfigLookUp(MavenSession session) {
        this.mavenExecutionRequest = session.getRequest();
        this.executionDir = new File(session.getExecutionRootDirectory());
    }

    File getFirstDirWithConfigOrProjectRootDir() {
        return getFirstDirWithConfigOrProjectRootDir(executionDir,
            new File(mavenExecutionRequest.getSystemProperties().getProperty("env.MAVEN_PROJECTBASEDIR")));
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
}
