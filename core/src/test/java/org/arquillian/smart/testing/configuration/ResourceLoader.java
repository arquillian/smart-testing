package org.arquillian.smart.testing.configuration;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

class ResourceLoader {

    static File getResourceAsFile(String resourceName) {
        final URL resource = getResource(resourceName);
        return new File(resource.getFile());
    }

    static Path getResourceAsPath(String resourceName) {
        try {
            return Paths.get(getResource(resourceName).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    static URL getResource(String resourceName) {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (resource == null) {
            throw new IllegalStateException("Expected " + resourceName + " to be on the classpath");
        }
        return resource;
    }
}
