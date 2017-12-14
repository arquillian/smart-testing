package org.arquillian.smart.testing.strategies.affected.fakeproject.test;

import org.arquillian.smart.testing.strategies.affected.WatchFile;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Test ignored because it is used internally")
@WatchFile("../changed/src/main/resources/META-INF/persistence.xml")
public class WTest {

    @Test
    public void dao_test() {
        System.out.println("JPA Test");
    }

}
