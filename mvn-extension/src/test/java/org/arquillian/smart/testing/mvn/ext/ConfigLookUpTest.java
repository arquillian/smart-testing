package org.arquillian.smart.testing.mvn.ext;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigLookUpTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    MavenSession mavenSession;

    private MavenExecutionRequest mavenExecutionRequest;

    @Before
    public void configureMavenExecutionRequest() {
        mavenExecutionRequest = new DefaultMavenExecutionRequest();
        Properties properties = new Properties();
        properties.put("env.MAVEN_PROJECTBASEDIR", temporaryFolder.getRoot().toString());
        mavenExecutionRequest.setSystemProperties(properties);
    }

    @Test
    public void should_return_first_dir_with_config_file() throws IOException {
        temporaryFolder.newFolder("parent", "config");
        temporaryFolder.newFile("parent" + File.separator + "pom.xml");
        temporaryFolder.newFile("parent" + File.separator + "smart-testing.yml");
        temporaryFolder.newFile(String.join(File.separator, "parent", "config", "pom.xml"));

        final String rootPath = temporaryFolder.getRoot().getPath();

        when(mavenSession.getExecutionRootDirectory()).thenReturn(
            String.join(File.separator, rootPath, "parent", "config"));
        when(mavenSession.getRequest()).thenReturn(mavenExecutionRequest);

        final ConfigLookUp configLookUp = new ConfigLookUp(mavenSession);

        final File firstDirWithConfigOrProjectRootDir = configLookUp.getFirstDirWithConfigOrProjectRootDir();

        Assertions.assertThat(firstDirWithConfigOrProjectRootDir)
            .isEqualTo(new File(rootPath + File.separator + "parent"));
    }

    @Test
    public void should_return_parent_dir_if_config_file_is_not_present() throws IOException {
        temporaryFolder.newFolder("parent", "config");
        temporaryFolder.newFile(String.join(File.separator, "parent", "pom.xml"));
        temporaryFolder.newFile(String.join(File.separator, "parent", "config", "pom.xml"));

        final String rootPath = temporaryFolder.getRoot().getPath();

        when(mavenSession.getExecutionRootDirectory()).thenReturn(
            String.join(File.separator, rootPath, "parent", "config"));
        when(mavenSession.getRequest()).thenReturn(mavenExecutionRequest);

        final ConfigLookUp configLookUp = new ConfigLookUp(mavenSession);

        final File firstDirWithConfigOrProjectRootDir = configLookUp.getFirstDirWithConfigOrProjectRootDir();

        Assertions.assertThat(firstDirWithConfigOrProjectRootDir).isEqualTo(temporaryFolder.getRoot());
    }
}
