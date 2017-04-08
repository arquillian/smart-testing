package org.arquillian.smart.testing.surefire.provider.info;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.surefire.booter.ProviderParameterNames;
import org.arquillian.smart.testing.surefire.provider.ProviderParametersParser;
import org.arquillian.smart.testing.surefire.provider.SurefireDependencyResolver;
import org.arquillian.smart.testing.surefire.provider.Validate;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class JUnitCoreProviderInfo extends JUnitProviderInfo {

    private ProviderParametersParser paramParser;

    public JUnitCoreProviderInfo(ProviderParametersParser paramParser) {
        super(paramParser.getJunitVersion());
        this.paramParser = paramParser;
    }

    public String getProviderClassName() {
        return "org.apache.maven.surefire.junitcore.JUnitCoreProvider";
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
            && paramParser.getSurefireApiVersion() != null;
    }

    public String getDepCoordinates() {
        return "org.apache.maven.surefire:surefire-junit47:" + paramParser.getSurefireApiVersion();
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
