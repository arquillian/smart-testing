package org.arquillian.smart.testing.strategies.failed;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.arquillian.smart.testing.hub.storage.local.TemporaryInternalFiles;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.arquillian.smart.testing.spi.TestResult;
import org.arquillian.smart.testing.spi.TestResultParser;

public class InProjectTestReportLoader implements TestReportLoader {

    private final JavaSPILoader javaSPILoader;
    private File rootDirectory;

    public InProjectTestReportLoader(JavaSPILoader javaSPILoader, File projectDir) {
        this.javaSPILoader = javaSPILoader;
        this.rootDirectory = projectDir;
    }

    InProjectTestReportLoader(JavaSPILoader javaSPILoader, String rootDirectory) {
        this(javaSPILoader, new File(rootDirectory));
    }

    @Override
    public Set<String> loadTestResults() {

        final Set<String> testResults = new HashSet<>();

        final Path reportDir = TemporaryInternalFiles.createTestReportDirAction(rootDirectory).getPath();

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
}
