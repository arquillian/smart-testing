package org.arquillian.smart.testing.strategies.categorized;

import java.util.ArrayList;
import java.util.List;
import org.arquillian.smart.testing.configuration.ConfigurationItem;
import org.arquillian.smart.testing.spi.StrategyConfiguration;

import static org.arquillian.smart.testing.strategies.categorized.CategorizedTestsDetector.CATEGORIZED;

public class CategorizedConfiguration implements StrategyConfiguration {

    private static final String SMART_TESTING_CATEGORIZED_CATEGORIES = "smart.testing.categorized.categories";
    private static final String SMART_TESTING_CATEGORIZED_MATCH_ALL = "smart.testing.categorized.match.all";
    private static final String SMART_TESTING_CATEGORIZED_CASE_SENSITIVE = "smart.testing.categorized.case.sensitive";
    private static final String SMART_TESTING_CATEGORIZED_REVERSED = "smart.testing.categorized.reversed";
    private String[] categories;
    private boolean matchAll;
    private boolean caseSensitive;
    private boolean reversed;

    @Override
    public String name() {
        return CATEGORIZED;
    }

    @Override
    public List<ConfigurationItem> registerConfigurationItems() {
        ArrayList<ConfigurationItem> items = new ArrayList<>();
        items.add(new ConfigurationItem("categories", SMART_TESTING_CATEGORIZED_CATEGORIES, new String[0]));
        items.add(new ConfigurationItem("matchAll", SMART_TESTING_CATEGORIZED_MATCH_ALL, false));
        items.add(new ConfigurationItem("caseSensitive", SMART_TESTING_CATEGORIZED_CASE_SENSITIVE, false));
        items.add(new ConfigurationItem("reversed", SMART_TESTING_CATEGORIZED_REVERSED, false));
        return items;
    }

    public String[] getCategories() {
        return categories;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }

    public boolean isMatchAll() {
        return matchAll;
    }

    public void setMatchAll(boolean matchAll) {
        this.matchAll = matchAll;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }
}
