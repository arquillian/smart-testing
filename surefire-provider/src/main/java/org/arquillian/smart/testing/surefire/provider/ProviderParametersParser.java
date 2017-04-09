package org.arquillian.smart.testing.surefire.provider;

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

    private String getVersion(String classpathUrl, String prefix) {
        String[] pathSplit = classpathUrl.split(prefix);
        if (pathSplit != null && pathSplit.length == 2) {
            return pathSplit[1].substring(0, pathSplit[1].indexOf("/"));
        }
        return null;
    }

    public String getProperty(String key) {
        return providerParameters.getProviderProperties().get(key);
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
}
