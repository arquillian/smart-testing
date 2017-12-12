package org.arquillian.smart.testing.strategies.categorized.project;

import org.arquillian.smart.testing.strategies.categorized.project.categories.FirstCategory;
import org.arquillian.smart.testing.strategies.categorized.project.categories.SecondCategory;
import org.junit.experimental.categories.Category;

@Category({FirstCategory.class, SecondCategory.class})
public class FirstAndSecondCategorizedClass {
}
