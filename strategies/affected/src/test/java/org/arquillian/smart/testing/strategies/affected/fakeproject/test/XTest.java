package org.arquillian.smart.testing.strategies.affected.fakeproject.test;

import org.arquillian.smart.testing.strategies.affected.WatchFile;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Test ignored because it is used internally")
//tag::docs[]
@WatchFile("src/main/resources/META-INF/persistence.xml")
public class XTest {
//end::docs[]
    @Test
    public void dao_test() {
        System.out.println("JPA Test");
    }

}
