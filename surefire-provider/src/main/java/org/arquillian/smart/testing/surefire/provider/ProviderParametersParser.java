package org.arquillian.smart.testing.surefire.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.maven.surefire.providerapi.ProviderParameters;

import static org.arquillian.smart.testing.surefire.provider.Validate.isNotEmpty;

public class ProviderParametersParser {

    private static final Pattern JUNIT_PATTERN = Pattern.compile(".*/junit/junit/.*/junit-.*\\.jar");
    private static final Pattern TEST_NG_PATTERN = Pattern.compile(".*/org/testng/testng/.*/testng-.*\\.jar");
    private static final Pattern SUREFIRE_BOOTER_PATTERN =
        Pattern.compile(".*/org/apache/maven/surefire/surefire-api/.*/surefire-api-.*\\.jar");

    private String junitVersion;
    private String testNgVersion;
    private List<String> includes;
    private List<String> excludes;

    private String surefireApiVersion;
    private final ProviderParameters providerParameters;

    public ProviderParametersParser(ProviderParameters providerParameters) {
        this.providerParameters = providerParameters;

        int i = 0;
        String classpathUrl = null;
        while (isNotEmpty(classpathUrl = getProperty("classPathUrl." + i++))) {

            if (JUNIT_PATTERN.matcher(classpathUrl).matches()) {
                junitVersion = getVersion(classpathUrl, "/junit/junit/");
            } else if (TEST_NG_PATTERN.matcher(classpathUrl).matches()) {
                testNgVersion = getVersion(classpathUrl, "/org/testng/testng/");
            } else if (SUREFIRE_BOOTER_PATTERN.matcher(classpathUrl).matches()) {
                surefireApiVersion = getVersion(classpathUrl, "org/apache/maven/surefire/surefire-api/");
            }
        }
    }

    public List<String> getIncludes() {
        if (includes == null) {
            includes = getParameterList("includes");
        }
        return includes;
    }

    public List<String> getExcludes() {
        if (excludes == null) {
            excludes = getParameterList("excludes");
        }
        return excludes;
    }

    private List<String> getParameterList(String parameterKeyPrefix) {
        List<String> paramList = new ArrayList<>();

        int i = 0;
        String includesPattern = null;
        while (isNotEmpty(includesPattern = getProperty(parameterKeyPrefix + i++))) {
            paramList.add(includesPattern);
        }
        return paramList;
    }

    private String getVersion(String classpathUrl, String prefix) {
        String[] pathSplit = classpathUrl.split(prefix);
        if (pathSplit.length == 2) {
            return pathSplit[1].substring(0, pathSplit[1].indexOf("/"));
        }
        return null;
    }

    public String getProperty(String key) {
        return trimMultiline(providerParameters.getProviderProperties().get(key));
    }

    public boolean containsProperty(String key) {
        return providerParameters.getProviderProperties().containsKey(key);
    }

    public String getJunitVersion() {
        return junitVersion;
    }

    public String getTestNgVersion() {
        return testNgVersion;
    }

    public String getSurefireApiVersion() {
        return surefireApiVersion;
    }

    private String trimMultiline(String toTrim) {
        if (toTrim == null) {
            return null;
        }
        final StringBuilder builder = new StringBuilder(toTrim.length());
        for (String token : toTrim.split("\\s+")) {
            if (token != null) {
                String trimmed = token.trim();
                if (!trimmed.isEmpty()) {
                    builder.append(trimmed).append(' ');
                }
            }
        }
        return builder.toString().trim();
    }
}
