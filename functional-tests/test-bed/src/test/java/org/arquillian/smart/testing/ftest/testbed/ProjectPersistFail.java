package org.arquillian.smart.testing.ftest.testbed;

import org.arquillian.smart.testing.ftest.TestBedTemplate;
import org.junit.Assert;
import org.junit.Test;

public class ProjectPersistFail extends TestBedTemplate {

    @Test
    public void should_fail() throws Exception {
        Assert.assertFalse(true);
    }
}
