package org.arquillian.smart.testing.surefire.provider;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.logger.Log;

import static java.lang.Thread.currentThread;

/**
 * Loads library versions from given classloader.
 */
public class LoaderVersionExtractor {

    public static final String ARTIFACT_ID_MAVEN_FAILSAFE_PLUGIN = "maven-failsafe-plugin";
    public static final String ARTIFACT_ID_MAVEN_SUREFIRE_PLUGIN = "maven-surefire-plugin";

    public static final MavenLibrary LIBRARY_SUREFIRE_API =
        new MavenLibrary("org.apache.maven.surefire", "surefire-api");
    public static final MavenLibrary LIBRARY_FAILSAFE_PLUGIN =
        new MavenLibrary("org.apache.maven.plugins", ARTIFACT_ID_MAVEN_FAILSAFE_PLUGIN);
    public static final MavenLibrary LIBRARY_JUNIT = new MavenLibrary("junit", "junit");
    public static final MavenLibrary LIBRARY_TEST_NG = new MavenLibrary("org.testng", "testng");
    private static final Logger logger = Log.getLogger();
    private static final Map<ClassLoader, Map<MavenLibrary, String>> loaderWithLibraryVersions = new HashMap<>();
    private static final List<MavenLibrary> initLibraries = new ArrayList<>();

    static {
        initLibraries.add(LIBRARY_SUREFIRE_API);
        initLibraries.add(LIBRARY_JUNIT);
        initLibraries.add(LIBRARY_TEST_NG);
        initLibraries.add(LIBRARY_FAILSAFE_PLUGIN);
    }

    public static String getSurefireApiVersion() {
        return getVersionFromClassLoader(LIBRARY_SUREFIRE_API, currentThread().getContextClassLoader());
    }

    public static String getFailsafePluginVersion() {
        return getVersionFromClassLoader(LIBRARY_FAILSAFE_PLUGIN, currentThread().getContextClassLoader());
    }

    public static String getJunitVersion() {
        return getVersionFromClassLoader(LIBRARY_JUNIT, currentThread().getContextClassLoader());
    }

    public static String getTestNgVersion() {
        return getVersionFromClassLoader(LIBRARY_TEST_NG, currentThread().getContextClassLoader());
    }

    /**
     * In the given classloader finds manifest file on a path matching the given groupId and artifactId;
     * when the file is matched, then it retrieves and returns a version.
     *
     * @param mavenLibrary
     *     Maven library to find
     * @param loader
     *     The classloader the library should be in
     *
     * @return Version retrieved from the matched path
     */
    public static String getVersionFromClassLoader(MavenLibrary mavenLibrary, ClassLoader loader) {
        if (loaderWithLibraryVersions.get(loader) != null) {
            if (!initLibraries.contains(mavenLibrary)) {
                List<MavenLibrary> wrappedLibrary = Collections.singletonList(mavenLibrary);
                Map<MavenLibrary, String> implTitleWithVersion = getTitleWithVersion(wrappedLibrary, loader);
                loaderWithLibraryVersions.put(loader, implTitleWithVersion);
            }
        } else {
            Map<MavenLibrary, String> implTitleWithVersion = getTitleWithVersion(initLibraries, loader);
            loaderWithLibraryVersions.put(loader, implTitleWithVersion);
        }

        return loaderWithLibraryVersions.get(loader).get(mavenLibrary);
    }

    private static Map<MavenLibrary, String> getTitleWithVersion(List<MavenLibrary> libraries, ClassLoader classLoader) {
        Map<MavenLibrary, String> implTitleWithVersion = new HashMap<>();
        ArrayList<MavenLibrary> librariesToFind = new ArrayList<>(libraries);
        try {
            Enumeration<URL> manifests = classLoader.getResources("META-INF/MANIFEST.MF");

            while (manifests.hasMoreElements()) {
                String manifestURL = manifests.nextElement().toString();

                Optional<MavenLibrary> matched =
                    librariesToFind.parallelStream()
                        .filter(library -> manifestURL.matches(library.getRegex()))
                        .findFirst();

                matched.ifPresent(mavenLibrary -> {
                    MavenLibrary matchedLibrary = mavenLibrary;
                    String startWithVersion = manifestURL.replaceAll(matchedLibrary.getLeadingRegex(), "");
                    String version = startWithVersion.substring(0, startWithVersion.indexOf(File.separator));
                    implTitleWithVersion.put(matchedLibrary, version);
                    librariesToFind.remove(matchedLibrary);
                });
            }
        } catch (Exception e) {
            logger.warn("Exception {0} occurred while resolving manifest files", e.getMessage());
        }

        return implTitleWithVersion;
    }

    static class MavenLibrary {
        private final String groupId;
        private final String artifactId;

        MavenLibrary(String groupId, String artifactId) {

            this.groupId = groupId;
            this.artifactId = artifactId;
        }

        String getRegex() {
            return Pattern.compile(getLeadingString()
                + ".*"
                + File.separator
                + artifactId
                + "-.*\\.jar.*").pattern();
        }

        String getLeadingRegex() {
            return Pattern.compile(getLeadingString()).pattern();
        }

        private String getLeadingString() {
            return ".*"
                + File.separator
                + groupId.replaceAll("\\.", File.separator)
                + File.separator
                + artifactId
                + File.separator;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final MavenLibrary that = (MavenLibrary) o;

            if (groupId != null ? !groupId.equals(that.groupId) : that.groupId != null) return false;
            return artifactId != null ? artifactId.equals(that.artifactId) : that.artifactId == null;
        }

        @Override
        public int hashCode() {
            int result = groupId != null ? groupId.hashCode() : 0;
            result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
            return result;
        }
    }
}
