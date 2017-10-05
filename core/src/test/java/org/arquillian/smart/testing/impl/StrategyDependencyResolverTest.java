package org.arquillian.smart.testing.impl;

import java.util.List;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.experimental.categories.Category;

import static org.assertj.core.api.Assertions.assertThat;

@Category(NotThreadSafe.class)
public class StrategyDependencyResolverTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void should_resolve_dependencies_based_on_default_property_file_when_nothing_more_is_specified() throws Exception {
        // given
        final StrategyDependencyResolver mavenStrategyDependencyResolver = new StrategyDependencyResolver();

        // when
        List<String> dependencies = mavenStrategyDependencyResolver.resolveStrategies();

        // then
        assertThat(dependencies).hasSize(3)
            .contains("affected",
                "failed",
                "new");
    }

}
