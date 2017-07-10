package org.arquillian.smart.testing.ftest.testbed.testresults;

public enum Status {

    PASSED, FAILURE, RE_RUN_FAILURE, SKIPPED, ERROR;

    static Status from(String statusCode) {
        switch (statusCode) {
            case "+":
                return PASSED;
            case "-":
                return FAILURE;
            case "!":
                return ERROR;
            case "@":
                return RE_RUN_FAILURE;
            case "x":
                return SKIPPED;
            default:
                throw new IllegalArgumentException("Unrecognized status code [" + statusCode + "]");
        }
    }
}
