package org.arquillian.smart.testing.mvn.ext.custom.assertions;

import org.arquillian.smart.testing.mvn.ext.checker.SkipInstallationChecker;
import org.assertj.core.api.AbstractAssert;

import java.util.Objects;

public class SkipInstallationCheckerAssert extends AbstractAssert<SkipInstallationCheckerAssert, SkipInstallationChecker> {

    SkipInstallationCheckerAssert(SkipInstallationChecker actual) {
        super(actual, SkipInstallationCheckerAssert.class);
    }

    public static SkipInstallationCheckerAssert assertThat(SkipInstallationChecker checker) {
        return new SkipInstallationCheckerAssert(checker);
    }

    public SkipInstallationCheckerAssert isSkipped(boolean skip) {
        isNotNull();

        if (!Objects.equals(actual.shouldSkip(), skip)) {
            failWithMessage("Expected skip to be <%s> but was <%s>", skip, actual.shouldSkip());
        }
        return this;
    }

    public SkipInstallationCheckerAssert forReason(String reason) {
        isNotNull();

        if (!actual.getReason().contains(reason)) {
            failWithMessage("Expected reason to contain <%s> but was <%s>", reason, actual.getReason());
        }
        return this;
    }
}
