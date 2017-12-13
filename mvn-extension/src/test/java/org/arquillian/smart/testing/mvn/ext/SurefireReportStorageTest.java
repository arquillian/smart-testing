package org.arquillian.smart.testing.mvn.ext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.arquillian.smart.testing.hub.storage.local.LocalStorage;
import org.arquillian.smart.testing.hub.storage.local.TemporaryInternalFiles;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static java.util.Collections.singletonList;
import static org.arquillian.smart.testing.mvn.ext.SurefireReportStorage.SUREFIRE_REPORTS_DIR_NAME;
import static org.arquillian.smart.testing.mvn.ext.SurefireReportStorage.copySurefireReports;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurefireReportStorageTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private Model project;
    private File surefireReportsDir;
    private File projectDir;

    @Before
    public void prepareMocks() throws IOException {
        surefireReportsDir = folder.newFolder(SUREFIRE_REPORTS_DIR_NAME);
        projectDir = folder.newFolder();

        project = mock(Model.class);
        when(project.getProjectDirectory()).thenReturn(projectDir);

        Build build = mock(Build.class);
        when(project.getBuild()).thenReturn(build);
        when(build.getDirectory()).thenReturn(surefireReportsDir.getParent());
    }

    @Test
    public void should_copy_xml_files() throws IOException {
        // given
        Map<String, File> expectedReports = feedWithReports(surefireReportsDir);

        // when
        copySurefireReports(project);

        // then
        File reportsDir = TemporaryInternalFiles.createTestReportDirAction(projectDir).getFile();
        softly.assertThat(reportsDir).exists();

        Arrays.stream(reportsDir.listFiles()).forEach(file -> {
            softly.assertThat(expectedReports).containsKey(file.getName());
            softly.assertThat(file).hasSameContentAs(expectedReports.get(file.getName()));
        });
    }

    @Test
    public void should_purge_reports_directory() throws IOException {
        // given
        copySurefireReports(project);
        MavenSession mavenSession = mock(MavenSession.class);
        MavenProject mavenProject = mock(MavenProject.class);
        when(mavenProject.getModel()).thenReturn(project);
        when(mavenSession.getAllProjects()).thenReturn(singletonList(mavenProject));
        File reportsDir = TemporaryInternalFiles.createTestReportDirAction(projectDir).getFile();

        // when
        new LocalStorage(projectDir).duringExecution().purge(null);

        // then
        softly.assertThat(reportsDir).doesNotExist();
    }

    @Test
    public void should_not_create_nor_copy_anything_if_surefire_reports_dir_does_not_exist() throws IOException {
        // given
        surefireReportsDir.delete();

        // when
        copySurefireReports(project);

        // then
        File reportsDir = TemporaryInternalFiles.createTestReportDirAction(projectDir).getFile();
        softly.assertThat(reportsDir).doesNotExist();
    }

    private Map<String, File> feedWithReports(File surefireReportsDir) throws IOException {
        Map<String, File> expectedReports = new HashMap<>();
        expectedReports.put("first-report.xml", createDummyFile(surefireReportsDir, "first-report.xml"));
        expectedReports.put("second-report.xml", createDummyFile(surefireReportsDir, "second-report.xml"));
        expectedReports.put("third-report.xml", createDummyFile(surefireReportsDir, "third-report.xml"));
        createDummyFile(surefireReportsDir, "not-xml-report");
        return expectedReports;
    }

    private File createDummyFile(File directory, String fileName) throws IOException {
        File file = new File(directory, fileName);
        Files.write(file.toPath(), fileName.getBytes());
        return file;
    }
}
