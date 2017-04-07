package org.arquillian.smart.testing.strategies.affected;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleImportsClass {

    public List<URL> getUrls(String initial) throws MalformedURLException {

        return new ArrayList<>(Arrays.asList(new URL("http://localhost")));
    }

}
