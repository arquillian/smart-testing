package org.arquillian.smart.testing.surefire.provider;

import java.util.regex.Pattern;
import org.apache.maven.surefire.providerapi.ProviderParameters;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class ProviderParametersParser {

    private String junitVersion;
    private String testNgVersion;
    private String surefireApiVersion;

    private final ProviderParameters providerParameters;

    public ProviderParametersParser(ProviderParameters providerParameters) {
        this.providerParameters = providerParameters;

        Pattern junitPattern = Pattern.compile(".*\\/junit\\/junit\\/.*\\/junit-.*\\.jar");
        Pattern testNgPattern = Pattern.compile(".*\\/org\\/testng\\/testng\\/.*\\/testng-.*\\.jar");
        Pattern surefireBooterPattern =
            Pattern.compile(".*\\/org\\/apache\\/maven\\/surefire\\/surefire-api\\/.*\\/surefire-api-.*\\.jar");

        int i = 0;
        String cpUrl = null;
        while (Validate.isNotEmpty(cpUrl = getProperty("classPathUrl." + i++))) {

            if (junitPattern.matcher(cpUrl).matches()) {
                junitVersion = getVersion(cpUrl, "/junit/junit/");
            } else if (testNgPattern.matcher(cpUrl).matches()) {
                testNgVersion = getVersion(cpUrl, "/org/testng/testng/");
            } else if (surefireBooterPattern.matcher(cpUrl).matches()) {
                surefireApiVersion = getVersion(cpUrl, "org/apache/maven/surefire/surefire-api/");
            }
        }
    }

    private String getVersion(String cpUrl, String prefix) {
        String[] pathSplit = cpUrl.split(prefix);
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
