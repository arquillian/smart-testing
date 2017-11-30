package org.arquillian.smart.testing.configuration;

import java.io.IOException;
import java.nio.file.Paths;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import static org.arquillian.smart.testing.RunMode.ORDERING;
import static org.arquillian.smart.testing.configuration.Configuration.SMART_TESTING;
import static org.arquillian.smart.testing.configuration.ConfigurationFileBuilder.configurationFile;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.SMART_TESTING_YML;
import static org.arquillian.smart.testing.configuration.ConfigurationOverwriteUsingInheritTest.CONFIG;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;
import static org.arquillian.smart.testing.scm.ScmRunnerProperties.SCM_LAST_CHANGES;
import static org.assertj.core.api.Assertions.assertThat;

@Category(NotThreadSafe.class)
public class ConfigurationOverwriteUsingInheritWithSystemPropertyTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void system_properties_should_take_precedence_over_config_file() throws IOException {
        // given
        System.setProperty(SMART_TESTING, "changed");
        System.setProperty(SCM_LAST_CHANGES, "3");

        temporaryFolder.newFolder(CONFIG);
        final String root = temporaryFolder.getRoot().toString();

        configurationFile()
            .inherit("../smart-testing.yml")
            .mode("ordering")
            .writeTo(Paths.get(root, CONFIG, SMART_TESTING_YML));

        configurationFile()
            .strategies("new, changed, affected")
            .writeTo(Paths.get(root, SMART_TESTING_YML));

        final Range range = new Range();
        range.setHead(HEAD);
        range.setTail(HEAD + "~3");

        // when
        final Configuration configuration = ConfigurationLoader.load(Paths.get(root, CONFIG).toFile());

        // then
        assertThat(configuration.getStrategies()).isEqualTo(new String[] {"changed"});
        assertThat(configuration.getScm().getRange()).isEqualToComparingFieldByField(range);
        assertThat(configuration.getMode()).isEqualTo(ORDERING);
    }
}
