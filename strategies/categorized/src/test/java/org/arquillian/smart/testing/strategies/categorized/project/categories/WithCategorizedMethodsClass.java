package org.arquillian.smart.testing.strategies.categorized.project.categories;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class WithCategorizedMethodsClass {

    @Test
    @Category(FirstCategory.class)
    public void testWithFirstCategory() {
    }

    @Test
    @Category(SecondCategory.class)
    public void testWithSecondCategory() {
    }

    @Test
    @Category(ThirdCategory.class)
    public void testWithThirdCategory() {
    }

    @Test
    public void testWithoutCategory() {
    }
}
