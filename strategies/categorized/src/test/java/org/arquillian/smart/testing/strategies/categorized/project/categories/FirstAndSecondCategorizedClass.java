package org.arquillian.smart.testing.strategies.categorized.project.categories;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({FirstCategory.class, SecondCategory.class})
public class FirstAndSecondCategorizedClass {

    @Test
    public void test() {
    }
}
