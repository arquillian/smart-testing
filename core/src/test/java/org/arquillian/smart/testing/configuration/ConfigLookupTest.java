package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

@Category(NotThreadSafe.class)
public class ConfigLookupTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Before
    public void configureMavenExecutionRequest() {
        environmentVariables.set("MAVEN_PROJECTBASEDIR", temporaryFolder.getRoot().toString());
    }

    @Test
    public void should_return_first_dir_with_config_file() throws IOException {
        // given
        temporaryFolder.newFolder("parent", "config");
        temporaryFolder.newFile(Paths.get("parent", "pom.xml").toString());
        temporaryFolder.newFile(Paths.get("parent", "smart-testing.yml").toString());
        temporaryFolder.newFile(Paths.get("parent", "config", "pom.xml").toString());

        final String rootPath = temporaryFolder.getRoot().getPath();

        File dirToStart = Paths.get(rootPath, "parent", "config").toFile();
        final ConfigLookup configLookUp = new ConfigLookup(dirToStart, this::isProjectRootDirectory);

        // when
        final File firstDirWithConfigOrProjectRootDir = configLookUp.getFirstDirWithConfigOrWithStopCondition();

        // then
        assertThat(firstDirWithConfigOrProjectRootDir)
            .isEqualTo(Paths.get(rootPath, "parent").toFile());
    }

    @Test
    public void should_return_parent_dir_if_config_file_is_not_present() throws IOException {
        // given
        temporaryFolder.newFolder("parent", "config");
        temporaryFolder.newFile(Paths.get("parent", "pom.xml").toString());
        temporaryFolder.newFile(Paths.get("parent", "config", "pom.xml").toString());

        final String rootPath = temporaryFolder.getRoot().getPath();

        File dirToStart = Paths.get(rootPath, "parent", "config").toFile();
        final ConfigLookup configLookUp = new ConfigLookup(dirToStart, this::isProjectRootDirectory);

        // when
        final File firstDirWithConfigOrProjectRootDir = configLookUp.getFirstDirWithConfigOrWithStopCondition();

        // then
        assertThat(firstDirWithConfigOrProjectRootDir).isEqualTo(temporaryFolder.getRoot());
    }


    private boolean isProjectRootDirectory(File dir) {
        try {
            return Files.isSameFile(dir.toPath(), temporaryFolder.getRoot().toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
