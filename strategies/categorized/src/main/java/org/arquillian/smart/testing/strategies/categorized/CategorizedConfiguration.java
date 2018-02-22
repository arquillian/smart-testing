package org.arquillian.smart.testing.strategies.categorized;

import java.util.ArrayList;
import java.util.List;
import org.arquillian.smart.testing.configuration.ConfigurationItem;
import org.arquillian.smart.testing.spi.StrategyConfiguration;

import static org.arquillian.smart.testing.strategies.categorized.CategorizedTestsDetector.CATEGORIZED;

public class CategorizedConfiguration implements StrategyConfiguration {

    private static final String SMART_TESTING_CATEGORIZED_CATEGORIES = "smart.testing.categorized.categories";
    private static final String SMART_TESTING_CATEGORIZED_EXCLUDED_CATEGORIES = "smart.testing.categorized.excluded.categories";
    private static final String SMART_TESTING_CATEGORIZED_CASE_SENSITIVE = "smart.testing.categorized.case.sensitive";
    private static final String SMART_TESTING_CATEGORIZED_METHODS = "smart.testing.categorized.methods";
    private String[] excludedCategories;
    private String[] categories;
    private boolean caseSensitive;
    private boolean methods;

    @Override
    public String name() {
        return CATEGORIZED;
    }

    @Override
    public List<ConfigurationItem> registerConfigurationItems() {
        ArrayList<ConfigurationItem> items = new ArrayList<>();
        items.add(new ConfigurationItem("categories", SMART_TESTING_CATEGORIZED_CATEGORIES, new String[0]));
        items.add(new ConfigurationItem("excludedCategories", SMART_TESTING_CATEGORIZED_EXCLUDED_CATEGORIES, new String[0]));
        items.add(new ConfigurationItem("caseSensitive", SMART_TESTING_CATEGORIZED_CASE_SENSITIVE, false));
        items.add(new ConfigurationItem("methods", SMART_TESTING_CATEGORIZED_METHODS, true));
        return items;
    }

    public String[] getCategories() {
        return categories;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }

    public String[] getExcludedCategories() {
        return excludedCategories;
    }

    public void setExcludedCategories(String[] excludedCategories) {
        this.excludedCategories = excludedCategories;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isMethods() {
        return methods;
    }

    public void setMethods(boolean methods) {
        this.methods = methods;
    }
}
