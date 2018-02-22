package org.arquillian.smart.testing.strategies.categorized.project.categories;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class WithIncludedAndExcludedCategoriesOnMethodsClass {

    @Test
    @Category(FirstCategory.class)
    public void testWithFirstCategory() {

    }

    @Test
    @Category({FirstCategory.class, SecondCategory.class})
    public void testWithFirstAndSecondCategory() {

    }

    @Test
    @Category({FirstCategory.class, ThirdCategory.class})
    public void testWithFirstAndThirdCategory() {

    }

    @Test
    @Category({SecondCategory.class, ThirdCategory.class})
    public void testWithSecondAndThirdCategory() {

    }

}
