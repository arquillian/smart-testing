package org.arquillian.smart.testing.ftest.testbed.testresults;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class TestResultsExtractor {

    private static final String TEST_RESULT_ENTRY = "(?m)^\\s*([+|\\-|!|@|x]).*$";
    private static final Pattern TEST_RESULT_ENTRY_PATTERN = Pattern.compile(TEST_RESULT_ENTRY);

    private final Repository repository;

    public TestResultsExtractor(Repository repository) {
        this.repository = repository;
    }

    /**
     * Loads expected test results defined in the commit message.
     *
     * In order to be able to extract expected test results, commit message should contain list of tests prefixed with
     * the status in separated lines, e.g.:
     *
     * + org.arquillian.PassingTest
     * - org.arquillian.FailingTest
     * @ org.arquillian.RerunTest
     * ! org.arquillian.ErrorTest
     * x org.arquillian.SkippedTest
     *
     * Each line can either be:
     *   - fully qualified test class name, which implies all test methods resulted with the aggregated status
     *   - fully qualified test class name and test method name (e.g. org.arquillian.SingleTest#firstTest)
     *     which defines expected result for a single test execution
     *
     * @see Status for all possible prefixes
     *
     * @param commitId
     * @return list of expected test results
     */
    public Collection<TestResult> expectedTestResults(ObjectId commitId) {
        final Set<TestResult> testResultExpectations = new HashSet<>();
        try (RevWalk revWalk = new RevWalk(repository)) {
            final RevCommit commit = revWalk.parseCommit(commitId);
            testResultExpectations.addAll(extractTestResultsExpectations(commit));
            revWalk.dispose();
        } catch (IOException e) {
            throw new RuntimeException("Failed extracting expected test results", e);
        }

        return testResultExpectations;
    }

    private Collection<TestResult> extractTestResultsExpectations(RevCommit commit) {
        final Set<TestResult> testResultExpectations = new HashSet<>();
        final String fullMessage = commit.getFullMessage();

        final Matcher matcher = TEST_RESULT_ENTRY_PATTERN.matcher(fullMessage);
        while (matcher.find()) {
            final String[] testResultParts = matcher.group().trim().split(" ");
            testResultExpectations.add(TestResult.from(testResultParts));
        }
        return testResultExpectations;
    }
}
