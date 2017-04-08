package org.arquillian.smart.testing.surefire.provider.info;

import javax.annotation.Nonnull;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.surefire.booter.ProviderParameterNames;
import org.arquillian.smart.testing.surefire.provider.ProviderParametersParser;
import org.arquillian.smart.testing.surefire.provider.SurefireDependencyResolver;
import org.arquillian.smart.testing.surefire.provider.Validate;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public final class JUnitCoreProviderInfo extends JUnitProviderInfo {

    private ProviderParametersParser paramParser;

    public JUnitCoreProviderInfo(ProviderParametersParser paramParser) {
        super(paramParser.getJunitVersion());
        this.paramParser = paramParser;
    }

    @Nonnull
    public String getProviderClassName() {
        return "org.apache.maven.surefire.junitcore.JUnitCoreProvider";
    }

    private boolean is47CompatibleJunitDep() {
        return getJunitDepArtifact() != null && isJunit47Compatible(getJunitDepArtifact());
    }

    public boolean isApplicable() {
        Artifact junitDepArtifact = getJunitDepArtifact();
        if (junitDepArtifact == null) {
            return false;
        }
        final boolean isJunitArtifact47 = isAnyJunit4(junitDepArtifact) && isJunit47Compatible(junitDepArtifact);
        final boolean isAny47ProvidersForcers = isAnyConcurrencySelected() || isAnyGroupsSelected();
        return isAny47ProvidersForcers && (isJunitArtifact47 || is47CompatibleJunitDep())
            && paramParser.getSurefireBooterVersion() != null;
    }

    public String getDepCoordinates() {
        return "org.apache.maven.surefire:surefire-junit47:" + paramParser.getSurefireBooterVersion();
    }

    private boolean isJunit47Compatible(Artifact artifact) {
        return SurefireDependencyResolver.isWithinVersionSpec(artifact, "[4.7,)");
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
