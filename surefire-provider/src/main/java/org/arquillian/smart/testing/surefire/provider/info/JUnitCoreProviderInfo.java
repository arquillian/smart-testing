package org.arquillian.smart.testing.surefire.provider.info;

import shaded.org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.surefire.booter.ProviderParameterNames;
import org.arquillian.smart.testing.surefire.provider.LoaderVersionExtractor;
import org.arquillian.smart.testing.surefire.provider.ProviderParametersParser;
import org.arquillian.smart.testing.surefire.provider.SurefireDependencyResolver;
import org.arquillian.smart.testing.surefire.provider.Validate;

import static org.arquillian.smart.testing.known.surefire.providers.KnownProvider.JUNIT_47;

public class JUnitCoreProviderInfo extends JUnitProviderInfo {

    private final ProviderParametersParser paramParser;

    public JUnitCoreProviderInfo(ProviderParametersParser paramParser) {
        super(LoaderVersionExtractor.getJunitVersion());
        this.paramParser = paramParser;
    }

    public String getProviderClassName() {
        return JUNIT_47.getProviderClassName();
    }

    private boolean is47CompatibleJunitDep() {
        return getJunitDepVersion() != null && isJunit47Compatible(getJunitDepVersion());
    }

    public boolean isApplicable() {
        ArtifactVersion junitDepVersion = getJunitDepVersion();
        if (junitDepVersion == null) {
            return false;
        }
        final boolean isJunitArtifact47 = isAnyJunit4() && isJunit47Compatible(junitDepVersion);
        final boolean isAny47ProvidersForcers = isAnyConcurrencySelected() || isAnyGroupsSelected();
        return isAny47ProvidersForcers && (isJunitArtifact47 || is47CompatibleJunitDep())
            && LoaderVersionExtractor.getSurefireApiVersion() != null;
    }

    public String getDepCoordinates() {
        return String.join(":", JUNIT_47.getGroupId(), JUNIT_47.getArtifactId(),
            LoaderVersionExtractor.getSurefireApiVersion());
    }

    private boolean isJunit47Compatible(ArtifactVersion artifactVersion) {
        return SurefireDependencyResolver.isWithinVersionSpec(artifactVersion, "[4.7,)");
    }

    protected boolean isAnyConcurrencySelected() {
        String parallel = paramParser.getProperty("parallel");
        return parallel != null && parallel.trim().length() > 0 && !parallel.equals("none");
    }

    protected boolean isAnyGroupsSelected() {
        String groups = paramParser.getProperty(ProviderParameterNames.TESTNG_GROUPS_PROP);
        String excludeGroups = paramParser.getProperty(ProviderParameterNames.TESTNG_EXCLUDEDGROUPS_PROP);
        return Validate.isNotEmpty(groups) || Validate.isNotEmpty(excludeGroups);
    }
}
