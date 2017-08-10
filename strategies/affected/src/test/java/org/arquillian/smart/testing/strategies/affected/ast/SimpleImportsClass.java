package org.arquillian.smart.testing.strategies.affected.ast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleImportsClass {

    public List<URL> getUrls(String initial) throws MalformedURLException {

        return new ArrayList<>(Collections.singletonList(new URL("http://localhost")));
    }

}
