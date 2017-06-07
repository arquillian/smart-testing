package org.arquillian.smart.testing.surefire.provider;

import java.util.ArrayList;
import java.util.List;
import org.apache.maven.surefire.providerapi.ProviderParameters;

import static org.arquillian.smart.testing.surefire.provider.Validate.isNotEmpty;

public class ProviderParametersParser {

    private List<String> includes;
    private List<String> excludes;

    private final ProviderParameters providerParameters;

    public ProviderParametersParser(ProviderParameters providerParameters) {
        this.providerParameters = providerParameters;
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

    public String getProperty(String key) {
        return trimMultiline(providerParameters.getProviderProperties().get(key));
    }

    public boolean containsProperty(String key) {
        return providerParameters.getProviderProperties().containsKey(key);
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
