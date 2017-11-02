package org.arquillian.smart.testing.strategies.affected;

import java.util.ArrayList;
import java.util.List;
import org.arquillian.smart.testing.configuration.ConfigurationItem;
import org.arquillian.smart.testing.spi.StrategyConfiguration;

import static org.arquillian.smart.testing.strategies.affected.AffectedTestsDetector.AFFECTED;

public class AffectedConfiguration implements StrategyConfiguration {

    private static final String SMART_TESTING_AFFECTED_TRANSITIVITY = "smart.testing.affected.transitivity";
    private static final String SMART_TESTING_AFFECTED_EXCLUSIONS = "smart.testing.affected.exclusions";
    private static final String SMART_TESTING_AFFECTED_INCLUSIONS = "smart.testing.affected.inclusions";
    private static final String DEFAULT_SMART_TESTING_AFFECTED_TRANSITIVITY_VALUE = "true";

    private boolean transitivity = true;
    private List<String> exclusions;
    private List<String> inclusions;

    public boolean isTransitivity() {
        return transitivity;
    }

    public void setTransitivity(boolean transitivity) {
        this.transitivity = transitivity;
    }

    public List<String> getExclusions() {
        return exclusions;
    }

    public void setExclusions(List<String> exclusions) {
        this.exclusions = exclusions;
    }

    public List<String> getInclusions() {
        return inclusions;
    }

    public void setInclusions(List<String> inclusions) {
        this.inclusions = inclusions;
    }

    @Override
    public List<ConfigurationItem> registerConfigurationItems() {
        List<ConfigurationItem> configItems = new ArrayList<>();
        configItems.add(new ConfigurationItem("transitivity", SMART_TESTING_AFFECTED_TRANSITIVITY,
            Boolean.valueOf(DEFAULT_SMART_TESTING_AFFECTED_TRANSITIVITY_VALUE)));
        configItems.add(new ConfigurationItem("exclusions", SMART_TESTING_AFFECTED_EXCLUSIONS));
        configItems.add(new ConfigurationItem("inclusions", SMART_TESTING_AFFECTED_INCLUSIONS));

        return configItems;
    }

    @Override
    public String name() {
        return AFFECTED;
    }
}
