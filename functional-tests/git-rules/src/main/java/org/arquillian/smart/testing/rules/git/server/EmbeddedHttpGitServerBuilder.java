package org.arquillian.smart.testing.rules.git.server;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.arquillian.smart.testing.rules.git.server.UrlNameExtractor.extractName;

public class EmbeddedHttpGitServerBuilder {

    private final Map<String, String> locations = new HashMap<>();

    private int port = EmbeddedHttpGitServer.definedPort();
    private boolean useAvailablePort = false;

    public EmbeddedHttpGitServerBuilder(String name, String location) {
        this.locations.put(name, location);
    }

    public EmbeddedHttpGitServerBuilder usingPort(int port) {
        this.port = port;
        return this;
    }

    public EmbeddedHttpGitServerBuilder fromBundle(String name, String bundleFile) {
        final EmbeddedHttpGitServerBuilder builder = EmbeddedHttpGitServer.fromBundle(name, bundleFile);
        mergeLocations(builder);
        return this;
    }

    public EmbeddedHttpGitServerBuilder fromBundle(String bundleFile) {
        final EmbeddedHttpGitServerBuilder builder = EmbeddedHttpGitServer.fromBundle(bundleFile, bundleFile);
        mergeLocations(builder);
        return this;
    }

    public EmbeddedHttpGitServerBuilder fromPath(Path path) {
        final EmbeddedHttpGitServerBuilder builder = EmbeddedHttpGitServer.fromPath(path);
        mergeLocations(builder);
        return this;
    }

    public EmbeddedHttpGitServerBuilder fromPath(String name, Path path) {
        final EmbeddedHttpGitServerBuilder builder = EmbeddedHttpGitServer.fromFile(name, path.toFile());
        mergeLocations(builder);
        return this;
    }

    public EmbeddedHttpGitServerBuilder fromFile(File file) {
        final EmbeddedHttpGitServerBuilder
            builder = EmbeddedHttpGitServer.fromFile(extractName(file.getAbsolutePath()), file);
        mergeLocations(builder);
        return this;
    }

    public EmbeddedHttpGitServerBuilder fromFile(String name, File file) {
        final EmbeddedHttpGitServerBuilder builder = EmbeddedHttpGitServer.fromFile(name, file);
        mergeLocations(builder);
        return this;
    }

    public EmbeddedHttpGitServerBuilder fromUrl(URL url) {
        final EmbeddedHttpGitServerBuilder builder =  fromUrl(extractName(url.toExternalForm()), url);
        mergeLocations(builder);
        return this;
    }

    public EmbeddedHttpGitServerBuilder fromUrl(String name, URL url) {
        final EmbeddedHttpGitServerBuilder builder = EmbeddedHttpGitServer.fromUrl(name, url);
        mergeLocations(builder);
        return this;
    }

    /**
     * If enabled it will find any free port to assign to the instance of the server
     */
    public EmbeddedHttpGitServerBuilder usingAnyFreePort() {
        this.useAvailablePort = true;
        return this;
    }

    public EmbeddedHttpGitServer create() {
        return new EmbeddedHttpGitServer(locations, port, useAvailablePort);
    }

    EmbeddedHttpGitServerBuilder mergeLocations(EmbeddedHttpGitServerBuilder builder) {
        builder.locations.forEach(this.locations::put);
        return this;
    }

}
