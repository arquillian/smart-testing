package org.arquillian.smart.testing.report;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.report.model.SmartTestingExecution;
import org.arquillian.smart.testing.report.model.TestConfiguration;

public class SmartTestingReportGenerator {

    static final String SMART_TESTING_REPORT_DIR = "smart.testing.report.dir";
    static final String SMART_TESTING_REPORT_NAME = "smart.testing.report.name";

    private final Collection<TestSelection> testSelections;
    private final Configuration configuration;
    private final String baseDir;

    public SmartTestingReportGenerator(Collection<TestSelection> testSelections, Configuration configuration, File baseDir) {
        this(testSelections, configuration, baseDir.getAbsolutePath());
    }

    public SmartTestingReportGenerator(Collection<TestSelection> testSelections, Configuration configuration, String baseDir) {
        this.testSelections = testSelections;
        this.configuration = configuration;
        this.baseDir = baseDir;
    }

    public void generateReport() {
        ExecutionReportMarshaller service =
            new ExecutionReportMarshaller(baseDir, System.getProperty(SMART_TESTING_REPORT_DIR),
                System.getProperty(SMART_TESTING_REPORT_NAME));
        service.marshal(getSmartTestingExecution(configuration));
    }

    private List<TestConfiguration> getTestConfigurations() {
        return testSelections.stream()
            .map(this::getTestConfiguration)
            .collect(Collectors.toList());
    }

    private TestConfiguration getTestConfiguration(TestSelection testSelection) {
        return TestConfiguration.builder()
            .withName(testSelection.getClassName())
            .withStrategies(testSelection.getTypes())
            .build();
    }

    private SmartTestingExecution getSmartTestingExecution(Configuration configuration) {
        return SmartTestingExecution.builder()
            .withModule(getModuleName())
            .addConfiguration()
                .withUsageMode(configuration.getMode().getName())
                .withStrategies(configuration.getStrategies())
                .withProperties(getSmartTestingProperties())
            .done()
            .addSelection()
                .withTestConfigurations(getTestConfigurations())
            .done()
            .build();
    }

    private String getModuleName() {
        return baseDir.substring(baseDir.lastIndexOf(File.separator) + 1);
    }

    private Map<String, String> getSmartTestingProperties() {
        final Properties properties = System.getProperties();

        return properties.stringPropertyNames()
            .stream()
            .filter(name -> name.startsWith("smart.testing"))
            .collect(Collectors.toMap(Function.identity(), properties::getProperty));
    }
}
