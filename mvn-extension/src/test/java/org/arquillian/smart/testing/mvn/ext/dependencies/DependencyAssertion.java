package org.arquillian.smart.testing.mvn.ext.dependencies;

import java.util.Objects;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;

public class DependencyAssertion extends AbstractAssert<DependencyAssertion, Dependency> {

    public DependencyAssertion(Dependency actual) {
        super(actual, DependencyAssertion.class);
    }

    public static DependencyAssertion assertThat(Dependency actual) {
        return new DependencyAssertion(actual);
    }

    public DependencyAssertion hasGroupId(String groupId) {
        isNotNull();
        if (!Objects.equals(actual.getGroupId(), groupId)) {
            failWithMessage("Expected dependency's groupId to be <%s> but was <%s>", groupId, actual.getGroupId());
        }
        return this;
    }

    public DependencyAssertion hasArtifactId(String artifactId) {
        isNotNull();
        if (!Objects.equals(actual.getArtifactId(), artifactId)) {
            failWithMessage("Expected dependency's artifactId to be <%s> but was <%s>", artifactId,
                actual.getArtifactId());
        }
        return this;
    }

    public DependencyAssertion hasVersion(String version) {
        isNotNull();
        if (!Objects.equals(actual.getVersion(), version)) {
            failWithMessage("Expected dependency's version to be <%s> but was <%s>", version, actual.getVersion());
        }
        return this;
    }

    public DependencyAssertion hasScope(String scope) {
        isNotNull();
        if (!Objects.equals(actual.getScope(), scope)) {
            failWithMessage("Expected dependency's scope to be <%s> but was <%s>", scope, actual.getScope());
        }
        return this;
    }

    public DependencyAssertion hasClassifier(String classifier) {
        isNotNull();
        if (!Objects.equals(actual.getClassifier(), classifier)) {
            failWithMessage("Expected dependency's classifier to be <%s> but was <%s>", classifier,
                actual.getClassifier());
        }
        return this;
    }

    public DependencyAssertion hasType(String type) {
        isNotNull();
        if (!Objects.equals(actual.getType(), type)) {
            failWithMessage("Expected dependency's type to be <%s> but was <%s>", type, actual.getType());
        }
        return this;
    }

    public ListAssert<Exclusion> exclusions() {
        isNotNull();
        return Assertions.assertThat(actual.getExclusions());
    }
}
