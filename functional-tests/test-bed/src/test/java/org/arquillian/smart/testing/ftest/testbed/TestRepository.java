package org.arquillian.smart.testing.ftest.testbed;

public class TestRepository {

    public static String testRepository() {
        return System.getProperty("test.bed.repo", "https://github.com/arquillian/smart-testing-dogfood-repo.git");
    }
}
