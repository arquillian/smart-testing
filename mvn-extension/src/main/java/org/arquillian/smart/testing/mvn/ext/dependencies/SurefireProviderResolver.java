package org.arquillian.smart.testing.mvn.ext.dependencies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.model.Dependency;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.known.surefire.providers.KnownProvider;

import static org.arquillian.smart.testing.known.surefire.providers.KnownProvider.JUNIT_4;
import static org.arquillian.smart.testing.known.surefire.providers.KnownProvider.JUNIT_47;
import static org.arquillian.smart.testing.known.surefire.providers.KnownProvider.JUNIT_5;
import static org.arquillian.smart.testing.known.surefire.providers.KnownProvider.TESTNG;

class SurefireProviderResolver {

    private static final Logger log = Log.getLogger();
    private final List<SurefireProviderDefinition> providers = new ArrayList<>();

    SurefireProviderResolver(Configuration configuration) {
        providers.add(createKnownProviderDependency(JUNIT_4));
        providers.add(createKnownProviderDependency(JUNIT_47));
        providers.add(createKnownProviderDependency(JUNIT_5));
        providers.add(createKnownProviderDependency(TESTNG));

        List<SurefireProviderDefinition> configuredProviders = Arrays.stream(configuration.getCustomProviders())
            .flatMap(this::createCustomSurefireProvider)
            .collect(Collectors.toList());
        providers.addAll(configuredProviders);
    }

    private SurefireProviderDefinition createKnownProviderDependency(KnownProvider knownProvider) {
        return new SurefireProviderDefinition(knownProvider.getGroupId(), knownProvider.getArtifactId(),
            knownProvider.getProviderClassName());
    }

    SurefireProviderDependency createSurefireProviderDepIfMatches(Dependency dependency) {
        return providers.stream()
            .filter(provider -> provider.matches(dependency))
            .map(provider -> new SurefireProviderDependency(dependency, provider))
            .findFirst()
            .orElse(null);
    }

    private Stream<SurefireProviderDefinition> createCustomSurefireProvider(String gaAndProviderClassNamePair) {
        String[] pair = gaAndProviderClassNamePair.split("=");
        if (pair.length == 2) {
            String[] ga = pair[0].split(":");
            if (ga.length >= 2) {
                return Stream.of(new SurefireProviderDefinition(ga[0], ga[1], pair[1]));
            }
        }
        log.warn(
            "The configured custom provider [%s] doesn't match the expected format: groupId:artifactId=fully.qualified.ProviderClassName",
            gaAndProviderClassNamePair);
        return Stream.empty();
    }

    class SurefireProviderDefinition {

        private final String groupId;
        private final String artifactId;
        private final String providerClassName;

        SurefireProviderDefinition(String groupId, String artifactId, String providerClassName) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.providerClassName = providerClassName;
        }

        boolean matches(Dependency dependency) {
            return groupId.equals(dependency.getGroupId()) && artifactId.equals(dependency.getArtifactId());
        }

        String getProviderClassName() {
            return providerClassName;
        }
    }
}
