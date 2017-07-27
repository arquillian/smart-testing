package org.arquillian.smart.testing.mvn.ext;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class VersionTest {

    @Parameterized.Parameters
    public static Collection<Object[]> artifactVersions() {
        return Arrays.asList(new Object[][] {
            { "2.0.0", "1.0.0", true }, { "2.0.0", "2.0.0", true }, { "2.1.0", "2.0.0", true }, { "2.0.1", "2.0.0", true },
            { "2.0.0", "2.0.0-SNAPSHOT", true }, { "2.0.0-rc.1", "2.0.0-SNAPSHOT", true }, { "2.0.0-rc.2", "2.0.0-rc.1", true },
            { "1.0.0", "2.0.0", false }, { "2.0.0", "2.1.0", false }, { "2.0.0", "2.0.1", false },
            { "2.0.0-SNAPSHOT", "2.0.0", false }, { "2.0.0-M1", "1.0.0", true }, { "2.0.0-M2", "2.0.0-M1", true }, {"2.19.1", "2.19.1", true}
        });
    }

    private Version version1;
    private Version version2;
    private boolean isGreater;

    public VersionTest(String version1, String version2, boolean isGreater) {
        this.version1 = Version.from(version1);
        this.version2 = Version.from(version2);
        this.isGreater = isGreater;
    }

    @Test
    public void version_1_should_be_greater_or_equal_to_version_2() {
        assertThat(version1.isGreaterOrEqualThan(version2)).isEqualTo(isGreater);
    }

}
