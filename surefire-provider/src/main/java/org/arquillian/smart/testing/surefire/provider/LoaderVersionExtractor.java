package org.arquillian.smart.testing.surefire.provider;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static java.lang.Thread.currentThread;

/**
 * Loads library versions from given classloader.
 */
public class LoaderVersionExtractor {

    private static final Logger logger = Logger.getLogger(LoaderVersionExtractor.class.getName());

    public static final MavenLibrary LIBRARY_SUREFIRE_BOOTER =
        new MavenLibrary("org.apache.maven.surefire", "surefire-booter");
    public static final MavenLibrary LIBRARY_JUNIT = new MavenLibrary("junit", "junit");
    public static final MavenLibrary LIBRARY_TEST_NG = new MavenLibrary("org.testng", "testng");

    private static Map<ClassLoader, Map<MavenLibrary, String>> loaderWithLibraryVersions = new HashMap<>();
    private static List<MavenLibrary> initLibraries = new ArrayList<>();

    static {
        initLibraries.add(LIBRARY_SUREFIRE_BOOTER);
        initLibraries.add(LIBRARY_JUNIT);
        initLibraries.add(LIBRARY_TEST_NG);
    }

    public static String getSurefireBooterVersion() {
        return getVersionFromClassLoader(LIBRARY_SUREFIRE_BOOTER, currentThread().getContextClassLoader());
    }

    public static String getJunitVersion() {
        return getVersionFromClassLoader(LIBRARY_JUNIT, currentThread().getContextClassLoader());
    }

    public static String getTestNgVersion() {
        return getVersionFromClassLoader(LIBRARY_TEST_NG, currentThread().getContextClassLoader());
    }

    /**
     * In the given classloader finds manifest file on a path matching the given groupId and artifactId;
     * when it the file is matched, then it retrieves and returns a version.
     *
     * @param mavenLibrary Maven library to find
     * @param loader The classloader the library should be in
     * @return Version retrieved from the matched path
     */
    public static String getVersionFromClassLoader(MavenLibrary mavenLibrary, ClassLoader loader) {
        if (loaderWithLibraryVersions.get(loader) != null) {
            if (!initLibraries.contains(mavenLibrary)) {
                List<MavenLibrary> wrappedLibrary = Arrays.asList(new MavenLibrary[] {mavenLibrary});
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
                    librariesToFind.parallelStream().filter(library -> manifestURL.matches(library.getRegex())).findFirst();

                if (matched.isPresent()) {
                    MavenLibrary matchedLibrary = matched.get();
                    String startWithVersion = manifestURL.replaceAll(matchedLibrary.getLeadingRegex(), "");
                    String version = startWithVersion.substring(0, startWithVersion.indexOf(File.separator));
                    implTitleWithVersion.put(matchedLibrary, version);
                    librariesToFind.remove(matchedLibrary);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING,
                "Exception {0} occurred while resolving manifest files",
                e.getMessage());
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
            if (artifactId != null ? !artifactId.equals(that.artifactId) : that.artifactId != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = groupId != null ? groupId.hashCode() : 0;
            result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
            return result;
        }
    }
}
