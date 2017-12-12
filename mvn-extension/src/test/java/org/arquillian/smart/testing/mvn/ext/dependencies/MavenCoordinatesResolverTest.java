package org.arquillian.smart.testing.mvn.ext.dependencies;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.arquillian.smart.testing.mvn.ext.dependencies.DependencyAssertion.assertThat;

public class MavenCoordinatesResolverTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private String groupId = "org.my.group.id";
    private String artifactId = "my-artifactId";
    private String version = "my-version";
    private String classifier = "my-classifier";
    private String packaging = "my-packaging";
    private String scope = "my-scope";

    @Test
    public void should_create_dependency_from_ga_with_st_version_without_exclusions() {
        // when
        Dependency dependency =
            MavenCoordinatesResolver.createDependencyFromCoordinates(groupId + ":" + artifactId, false);

        // then
        assertThat(dependency)
            .hasGroupId(groupId)
            .hasArtifactId(artifactId)
            .hasVersion(ExtensionVersion.version().toString())
            .hasType("jar")
            .hasClassifier(null)
            .exclusions().isEmpty();
    }

    @Test
    public void should_create_dependency_from_gav_with_exclusions() {
        // when
        Dependency dependency =
            MavenCoordinatesResolver.createDependencyFromCoordinates(String.join(":", groupId, artifactId, version),
                true);

        // then
        assertThat(dependency)
            .hasGroupId(groupId)
            .hasArtifactId(artifactId)
            .hasVersion(version)
            .hasType("jar")
            .hasClassifier(null)
            .exclusions().hasSize(1);
        Exclusion exclusion = dependency.getExclusions().get(0);
        Assertions.assertThat(exclusion.getGroupId()).isEqualTo("*");
        Assertions.assertThat(exclusion.getArtifactId()).isEqualTo("*");
    }

    @Test
    public void should_throw_exception_when_wrong_coordinates_are_set() {
        // given
        thrown.expect(IllegalArgumentException.class);

        // when
        MavenCoordinatesResolver.createDependencyFromCoordinates(groupId + artifactId, false);

        // then exception should be thrown
    }

    @Test
    public void should_create_dependency_from_ga_with_st_version_with_shaded_classifier() {
        // when
        Dependency dependency =
            MavenCoordinatesResolver.createDependencyFromCoordinates(
                String.join(":", groupId, artifactId, "", classifier), false);

        // then
        assertThat(dependency)
            .hasGroupId(groupId)
            .hasArtifactId(artifactId)
            .hasVersion(ExtensionVersion.version().toString())
            .hasType("jar")
            .hasClassifier(classifier)
            .exclusions().isEmpty();
    }

    @Test
    public void should_create_dependency_from_whole_coordinates() {
        // when
        Dependency dependency =
            MavenCoordinatesResolver.createDependencyFromCoordinates(
                String.join(":", groupId, artifactId, packaging, classifier, version,scope), false);

        // then
        assertThat(dependency)
            .hasGroupId(groupId)
            .hasArtifactId(artifactId)
            .hasVersion(version)
            .hasType(packaging)
            .hasClassifier(classifier)
            .hasScope(scope)
            .exclusions().isEmpty();
    }
}
