package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Function;

import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YAML;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YML;

public class ConfigLookup {

    private final File executionRootDir;
    private final Function<File, Boolean> stopCondition;

    public ConfigLookup(File executionRootDir, Function<File, Boolean> stopCondition) {
        this.executionRootDir = executionRootDir;
        this.stopCondition = stopCondition;
    }

    public File getFirstDirWithConfigOrWithStopCondition() {
        return getFirstDirWithConfigOrWithStopCondition(executionRootDir);
    }

    private File getFirstDirWithConfigOrWithStopCondition(File projectDir) {
        if (stopCondition.apply(projectDir)) {
            return projectDir;
        }

        if (projectDir.isDirectory() && new File(projectDir, SMART_TESTING_YML).exists() || new File(projectDir,
            SMART_TESTING_YAML).exists()) {
            return projectDir;
        }

        return getFirstDirWithConfigOrWithStopCondition(projectDir.getParentFile());
    }

    private boolean isSameFile(Path path1, Path path2) {
        try {
            return Files.isSameFile(path1, path2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isConfigFromProjectRootDir() {
        final File configOrProjectRootDir = getFirstDirWithConfigOrWithStopCondition();
        return isSameFile(configOrProjectRootDir.toPath(), executionRootDir.toPath());
    }

    public boolean hasMoreThanOneConfigFile(String... fileNames){
        final long count;
        try {
            count = Files.walk(Paths.get(executionRootDir.getAbsolutePath()))
                .parallel()
                .filter(p -> Arrays.asList(fileNames).contains(p.toFile().getName()))
                .count();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return count > 1;
    }
}
