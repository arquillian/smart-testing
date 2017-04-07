package org.arquillian.smart.testing.surefire.provider;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class ProviderList {

    /*private final ProviderInfo[] wellKnownProviders;

    private final ConfigurableProviderInfo dynamicProvider;

    private SurefireDependencyResolver dependencyResolver;*/


    ProviderList() {

       /* Artifact junitDepArtifact = getJunitDepArtifact();
        this.wellKnownProviders = new ProviderInfo[] {new TestNgProviderInfo(getTestNgArtifact()),
            new JUnitCoreProviderInfo(getJunitArtifact(), junitDepArtifact),
            new JUnit4ProviderInfo(getJunitArtifact(), junitDepArtifact),
            new JUnit3ProviderInfo()};
        this.dynamicProvider = new DynamicProviderInfo(null);*/

        //dependencyResolver =
        //    new SurefireDependencyResolver( getArtifactResolver(), getArtifactFactory(), getLog(), getLocalRepository(),
        //        getRemoteRepositories(), getMetadataSource(), getPluginName() );
    }
/*
    @SuppressWarnings("checkstyle:modifierorder")
    @Nonnull
    List<ProviderInfo> resolve() {
        List<ProviderInfo> providersToRun = new ArrayList<ProviderInfo>();
        //Set<String> manuallyConfiguredProviders = getManuallyConfiguredProviders();
        //for ( String name : manuallyConfiguredProviders )
        //{
        //    ProviderInfo wellKnown = findByName( name );
        //    ProviderInfo providerToAdd = wellKnown != null ? wellKnown : dynamicProvider.instantiate( name );
        //    logDebugOrCliShowErrors( "Using configured provider " + providerToAdd.getProviderName() );
        //    providersToRun.add( providerToAdd );
        //}
        //return manuallyConfiguredProviders.isEmpty() ? autoDetectOneProvider() : providersToRun;
        return autoDetectOneProvider();
    }

    @SuppressWarnings("checkstyle:modifierorder")
    private @Nonnull
    List<ProviderInfo> autoDetectOneProvider() {
        List<ProviderInfo> providersToRun = new ArrayList<ProviderInfo>();
        for (ProviderInfo wellKnownProvider : wellKnownProviders) {
            if (wellKnownProvider.isApplicable()) {
                providersToRun.add(wellKnownProvider);
                return providersToRun;
            }
        }
        return providersToRun;
    }

    //private Set<String> getManuallyConfiguredProviders()
    //{
    //    try
    //    {
    //        return ProviderDetector.getServiceNames( SurefireProvider.class,
    //            Thread.currentThread().getContextClassLoader() );
    //    }
    //    catch ( IOException e )
    //    {
    //        throw new RuntimeException( e );
    //    }
    //}

    private ProviderInfo findByName(String providerClassName) {
        for (ProviderInfo wellKnownProvider : wellKnownProviders) {
            if (wellKnownProvider.getProviderName().equals(providerClassName)) {
                return wellKnownProvider;
            }
        }
        return null;
    }

    private Artifact getJunitDepArtifact() {

        return new DefaultArtifact("junit", "junit", "version", "test", "jar", null, null);
        //return getProjectArtifactMap().get("junit:junit-dep");
    }

    public Artifact getTestNGDepArtifact() {
        return new DefaultArtifact("org.testng", "testng", "version", "test", "jar", null, null);
    }

    private Artifact getTestNgArtifact() {
        Artifact artifact = getTestNGDepArtifact();
        // todo - solve following code, but it should never happen - If I understand it correctly, this is only check for the case that surefire is used in testng project
        //Artifact projectArtifact = project.getArtifact();
        //String projectArtifactName = projectArtifact.getGroupId() + ":" + projectArtifact.getArtifactId();
        //
        //if (artifact != null) {
        //    VersionRange range = createVersionRange();
        //    if (!range.containsVersion(new DefaultArtifactVersion(artifact.getVersion()))) {
        //        throw new IllegalArgumentException(
        //            "TestNG support requires version 4.7 or above. You have declared version "
        //                + artifact.getVersion());
        //    }
        //} else if (projectArtifactName.equals(getTestNGArtifactName())) {
        //    artifact = projectArtifact;
        //}

        return artifact;
    }

    private Artifact getJunitArtifact() {
        Artifact artifact = getJunitDepArtifact();

        // todo - solve following code, but it should never happen - If I understand it correctly, this is only check for the case that surefire is used in junit project
        //Artifact projectArtifact = project.getArtifact();
        //String projectArtifactName = projectArtifact.getGroupId() + ":" + projectArtifact.getArtifactId();
        //
        //if (artifact == null && projectArtifactName.equals(getJunitArtifactName())) {
        //    artifact = projectArtifact;
        //}

        return artifact;
    }

    final class TestNgProviderInfo
        implements ProviderInfo {
        private final Artifact testNgArtifact;

        TestNgProviderInfo(Artifact testNgArtifact) {
            this.testNgArtifact = testNgArtifact;
        }

        @SuppressWarnings("checkstyle:modifierorder")
        public @Nonnull
        String getProviderName() {
            return "org.apache.maven.surefire.testng.TestNGProvider";
        }

        public boolean isApplicable() {
            return testNgArtifact != null;
        }

        public void addProviderProperties() throws MojoExecutionException {
            convertTestNGParameters();
        }

        public Classpath getProviderClasspath()
            throws ArtifactResolutionException, ArtifactNotFoundException {
            Artifact surefireArtifact = getPluginArtifactMap().get("org.apache.maven.surefire:surefire-booter");
            return dependencyResolver.getProviderClasspath("surefire-testng", surefireArtifact.getBaseVersion(),
                testNgArtifact);
        }
    }

    *//**
     * Converts old TestNG configuration parameters over to new properties based configuration
     * method. (if any are defined the old way)
     *//*
    private void convertTestNGParameters() throws MojoExecutionException
    {
        if ( this.getParallel() != null )
        {
            getProperties().setProperty( ProviderParameterNames.PARALLEL_PROP, this.getParallel() );
        }
        convertGroupParameters();

        if ( this.getThreadCount() > 0 )
        {
            getProperties().setProperty( ProviderParameterNames.THREADCOUNT_PROP,
                Integer.toString( this.getThreadCount() ) );
        }
        if ( this.getObjectFactory() != null )
        {
            getProperties().setProperty( "objectfactory", this.getObjectFactory() );
        }
        if ( this.getTestClassesDirectory() != null )
        {
            getProperties().setProperty( "testng.test.classpath", getTestClassesDirectory().getAbsolutePath() );
        }

        Artifact testNgArtifact = getTestNgArtifact();
        if ( testNgArtifact != null )
        {
            DefaultArtifactVersion defaultArtifactVersion = new DefaultArtifactVersion( testNgArtifact.getVersion() );
            getProperties().setProperty( "testng.configurator", getConfiguratorName( defaultArtifactVersion,
                getLog() ) );
        }
    }

    final class JUnit3ProviderInfo
        implements ProviderInfo {
        @Nonnull
        public String getProviderName() {
            return "org.apache.maven.surefire.junit.JUnit3Provider";
        }

        public boolean isApplicable() {
            return true;
        }

        public void addProviderProperties() throws MojoExecutionException {
        }

        public Classpath getProviderClasspath()
            throws ArtifactResolutionException, ArtifactNotFoundException {
            // add the JUnit provider as default - it doesn't require JUnit to be present,
            // since it supports POJO tests.
            return dependencyResolver.getProviderClasspath("surefire-junit3", surefireBooterArtifact.getBaseVersion(),
                null);
        }
    }

    final class JUnit4ProviderInfo
        implements ProviderInfo {
        private final Artifact junitArtifact;

        private final Artifact junitDepArtifact;

        JUnit4ProviderInfo(Artifact junitArtifact, Artifact junitDepArtifact) {
            this.junitArtifact = junitArtifact;
            this.junitDepArtifact = junitDepArtifact;
        }

        @Nonnull
        public String getProviderName() {
            return "org.apache.maven.surefire.junit4.JUnit4Provider";
        }

        public boolean isApplicable() {
            return junitDepArtifact != null || isAnyJunit4(junitArtifact);
        }

        public void addProviderProperties() throws MojoExecutionException {
        }

        public Classpath getProviderClasspath()
            throws ArtifactResolutionException, ArtifactNotFoundException {
            return dependencyResolver.getProviderClasspath("surefire-junit4", surefireBooterArtifact.getBaseVersion(),
                null);
        }
    }

    final class JUnitCoreProviderInfo
        implements ProviderInfo {
        private final Artifact junitArtifact;

        private final Artifact junitDepArtifact;

        JUnitCoreProviderInfo(Artifact junitArtifact, Artifact junitDepArtifact) {
            this.junitArtifact = junitArtifact;
            this.junitDepArtifact = junitDepArtifact;
        }

        @Nonnull
        public String getProviderName() {
            return "org.apache.maven.surefire.junitcore.JUnitCoreProvider";
        }

        private boolean is47CompatibleJunitDep() {
            return junitDepArtifact != null && isJunit47Compatible(junitDepArtifact);
        }

        public boolean isApplicable() {
            final boolean isJunitArtifact47 = isAnyJunit4(junitArtifact) && isJunit47Compatible(junitArtifact);
            final boolean isAny47ProvidersForcers = isAnyConcurrencySelected() || isAnyGroupsSelected();
            return isAny47ProvidersForcers && (isJunitArtifact47 || is47CompatibleJunitDep());
        }

        public void addProviderProperties() throws MojoExecutionException {
            convertJunitCoreParameters();
            convertGroupParameters();
        }

        public Classpath getProviderClasspath()
            throws ArtifactResolutionException, ArtifactNotFoundException {
            return dependencyResolver.getProviderClasspath("surefire-junit47", surefireBooterArtifact.getBaseVersion(),
                null);
        }
    }

    *//**
     * Provides the Provider information for manually configured providers.
     *//*
    final class DynamicProviderInfo
        implements ConfigurableProviderInfo {
        final String providerName;

        DynamicProviderInfo(String providerName) {
            this.providerName = providerName;
        }

        public ProviderInfo instantiate(String providerName) {
            return new DynamicProviderInfo(providerName);
        }

        @Nonnull
        public String getProviderName() {
            return providerName;
        }

        public boolean isApplicable() {
            return true;
        }

        public void addProviderProperties() throws MojoExecutionException {
            // Ok this is a bit lazy.
            convertJunitCoreParameters();
            convertTestNGParameters();
        }

        public Classpath getProviderClasspath()
            throws ArtifactResolutionException, ArtifactNotFoundException {
            final Map<String, Artifact> pluginArtifactMap = getPluginArtifactMap();
            Artifact plugin = pluginArtifactMap.get("org.apache.maven.plugins:maven-surefire-plugin");
            return dependencyResolver.addProviderToClasspath(pluginArtifactMap, plugin);
        }
    }*/
}
