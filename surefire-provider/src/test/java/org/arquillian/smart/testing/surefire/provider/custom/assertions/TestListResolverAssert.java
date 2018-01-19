package org.arquillian.smart.testing.surefire.provider.custom.assertions;

import java.util.Objects;
import org.apache.maven.surefire.testset.ResolvedTest;
import org.apache.maven.surefire.testset.TestListResolver;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.IterableAssert;

public class TestListResolverAssert extends AbstractAssert<TestListResolverAssert, TestListResolver> {

    TestListResolverAssert(TestListResolver actual) {
        super(actual, TestListResolverAssert.class);
    }

    public static TestListResolverAssert assertThat(TestListResolver customProviderInfo) {
        return new TestListResolverAssert(customProviderInfo);
    }

    public TestListResolverAssert hasIncludedMethodPatterns(boolean hasIncludedMethodPatterns) {
        isNotNull();

        if (!Objects.equals(actual.hasIncludedMethodPatterns(), hasIncludedMethodPatterns)) {
            failWithMessage("The given test list resolver <%s> should have <%s> included-method-patterns but it had <%s>",
                actual, not(hasIncludedMethodPatterns), not(actual.hasIncludedMethodPatterns()));
        }
        return this;
    }

    public TestListResolverAssert hasMethodPatterns(boolean hasMethodPatterns) {
        isNotNull();

        if (!Objects.equals(actual.hasMethodPatterns(), hasMethodPatterns)) {
            failWithMessage("The given test list resolver <%s> should have <%s> method-patterns but it had <%s>",
                actual, not(hasMethodPatterns), not(actual.hasMethodPatterns()));
        }
        return this;
    }

    public TestListResolverAssert hasExcludedMethodPatterns(boolean hasExcludedMethodPatterns) {
        isNotNull();

        if (!Objects.equals(actual.hasExcludedMethodPatterns(), hasExcludedMethodPatterns)) {
            failWithMessage("The given test list resolver <%s> should have <%s> excluded-method-patterns but it had <%s>",
                actual, not(hasExcludedMethodPatterns), not(actual.hasExcludedMethodPatterns()));
        }
        return this;
    }

    public IterableAssert<ResolvedTest> includedPatterns() {
        isNotNull();

        return Assertions.assertThat(actual.getIncludedPatterns());
    }

    public IterableAssert<ResolvedTest> excludedPatterns() {
        isNotNull();

        return Assertions.assertThat(actual.getExcludedPatterns());
    }

    private String not(boolean bool){
        return bool ? "" : "NOT";
    }
}
