package org.arquillian.smart.testing.spi;

public class TestResult {

    private final String className;
    private final String testMethod;
    private final Float testDuration;
    private Result result;

    public TestResult(String className, String testMethod, Float testDuration, Result result) {
        this.className = className;
        this.testMethod = testMethod;
        this.testDuration = testDuration;
        this.result = result;
    }

    public TestResult(String className, Result result) {
        this(className, "", 0f, result);
    }

    public TestResult(String className, String testMethod, Float testDuration) {
        this(className, testMethod, testDuration, Result.PASSED);
    }

    public String getClassName() {
        return className;
    }

    public String getTestMethod() {
        return testMethod;
    }

    public Float getTestDuration() {
        return testDuration;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public boolean isFailing() {
        return this.result == Result.ERROR || this.result == Result.FAILURE || this.result == Result.RE_RUN_FAILURE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TestResult that = (TestResult) o;

        if (className != null ? !className.equals(that.className) : that.className != null) return false;
        return testMethod != null ? testMethod.equals(that.testMethod) : that.testMethod == null;
    }

    @Override
    public int hashCode() {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (testMethod != null ? testMethod.hashCode() : 0);
        return result;
    }

    public enum Result {
        PASSED, FAILURE, RE_RUN_FAILURE, SKIPPED, ERROR
    }

}
