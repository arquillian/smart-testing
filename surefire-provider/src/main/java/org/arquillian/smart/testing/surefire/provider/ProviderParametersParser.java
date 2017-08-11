package org.arquillian.smart.testing.surefire.provider;

import org.apache.maven.surefire.providerapi.ProviderParameters;

public class ProviderParametersParser {

    private final ProviderParameters providerParameters;

    public ProviderParametersParser(ProviderParameters providerParameters) {
        this.providerParameters = providerParameters;
    }

    public String getProperty(String key) {
        return trimMultiline(providerParameters.getProviderProperties().get(key));
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

    public ProviderParameters getProviderParameters() {
        return providerParameters;
    }
}
