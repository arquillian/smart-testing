package org.arquillian.smart.testing.strategies.categorized.project.categories;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Included.class)
public class WithExcludedMethodClass {

    @Test
    public void testWithoutExcludedCategory() {

    }

    @Test
    @Category(Excluded.class)
    public void testWithExcludedCategory() {

    }

}
