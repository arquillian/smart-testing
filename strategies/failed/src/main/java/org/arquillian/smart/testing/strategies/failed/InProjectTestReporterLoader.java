package org.arquillian.smart.testing.strategies.failed;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestResult;
import org.arquillian.smart.testing.spi.TestResultParser;

public class InProjectTestReporterLoader implements TestReportLoader {

    private static final String IN_PROJECT_DIR = ".reports";

    private String inProjectDir = IN_PROJECT_DIR;
    private JavaSPILoader javaSPILoader;


    public InProjectTestReporterLoader(JavaSPILoader javaSPILoader) {
        this.javaSPILoader = javaSPILoader;
    }

    @Override
    public Set<String> loadTestResults() {

        final Set<String> testResults = new HashSet<>();

        final Path reportDir = Paths.get(".", inProjectDir);

        if (Files.exists(reportDir)) {

            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(reportDir)) {

                final Set<String> testFailures = StreamSupport.stream(directoryStream.spliterator(), false)
                    .map(path -> {
                        try {
                            return Files.newInputStream(path);
                        } catch (IOException e) {
                            throw new IllegalArgumentException(e);
                        }
                    })
                    .map(is -> {
                        final Optional<TestResultParser> testResultParser = javaSPILoader.onlyOne(TestResultParser.class);
                        if (!testResultParser.isPresent()) {
                            throw new IllegalArgumentException("No Test Result Parser found in classpath");
                        }

                        return testResultParser.get().parse(is);
                    })
                    .flatMap(Set::stream)
                    .filter(TestResult::isFailing)
                    .map(TestResult::getClassName)
                    .collect(Collectors.toSet());

                testResults.addAll(testFailures);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        return testResults;
    }

    void setInProjectDir(String inProjectDir) {
        this.inProjectDir = inProjectDir;
    }
}
