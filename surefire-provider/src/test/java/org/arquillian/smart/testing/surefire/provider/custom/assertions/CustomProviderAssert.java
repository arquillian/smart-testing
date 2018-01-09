package org.arquillian.smart.testing.surefire.provider.custom.assertions;

import org.arquillian.smart.testing.surefire.provider.info.CustomProviderInfo;
import org.assertj.core.api.AbstractAssert;

import java.util.Objects;

public class CustomProviderAssert extends AbstractAssert<CustomProviderAssert, CustomProviderInfo> {

    CustomProviderAssert(CustomProviderInfo actual) {
        super(actual, CustomProviderAssert.class);
    }

    public static CustomProviderAssert assertThat(CustomProviderInfo customProviderInfo) {
        return new CustomProviderAssert(customProviderInfo);
    }

    public CustomProviderAssert hasDepCoordinates(String depCoordinates) {
        isNotNull();

        if (!Objects.equals(actual.getDepCoordinates(), depCoordinates)) {
            failWithMessage("Expected Dependency Coordinates to be <%s> but was <%s>", depCoordinates, actual.getDepCoordinates());
        }
        return this;
    }

    public CustomProviderAssert hasProviderClassName(String providerClassName) {
        isNotNull();

        if (!Objects.equals(actual.getProviderClassName(), providerClassName)) {
            failWithMessage("Expected Provider ClassName to be <%s> but was <%s>", providerClassName, actual.getProviderClassName());
        }
        return this;
    }
}
