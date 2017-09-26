package org.arquillian.smart.testing.rules.git.server;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import org.junit.rules.ExternalResource;

import static org.arquillian.smart.testing.rules.git.server.UrlNameExtractor.extractName;

/**
 *
 * This rule starts a Git Web Server ({@link EmbeddedHttpGitServer}) serving Git repository imported from other location,
 * so it can be used while testing git-related logic (e.g. when using JGit as a client).
 *
 * To use it simply add following to your test class.
 *
 * <pre>
 * &#064;Rule
 * public final TestRule gitServer = GitServer.fromBundle("bundle.file").create();
 * </pre>
 *
 * @see EmbeddedHttpGitServer for details about the features.
 */
public class GitServer extends ExternalResource {

    private final EmbeddedHttpGitServer gitServer;

    private GitServer(EmbeddedHttpGitServer gitServer) {
        this.gitServer = gitServer;
    }

    @Override
    protected void before() throws Throwable {
        this.gitServer.start();
    }

    @Override
    protected void after() {
        this.gitServer.stop();
    }

    public int getPort() {
        return this.gitServer.getPort();
    }

    public static Builder fromBundle(String bundleFile) {
        return new Builder(EmbeddedHttpGitServer.fromBundle(bundleFile));
    }

    public static Builder fromBundle(String name, String bundleFile) {
        return new Builder(EmbeddedHttpGitServer.fromBundle(name, bundleFile));
    }

    public static Builder fromPath(Path path) {
        return new Builder(EmbeddedHttpGitServer.fromPath(path));
    }

    public static Builder fromFile(File file) {
        return new Builder(EmbeddedHttpGitServer.fromFile(file));
    }

    public static Builder fromUrl(URL url) {
        return new Builder(EmbeddedHttpGitServer.fromUrl(url));
    }

    public static Builder bundlesFromDirectory(String bundleDirectory) {
        return new Builder(EmbeddedHttpGitServer.bundlesFromDirectory(bundleDirectory));
    }

    public static class Builder {

        private final EmbeddedHttpGitServerBuilder builder;

        public Builder(EmbeddedHttpGitServerBuilder builder) {
            this.builder = builder;
        }

        public Builder usingPort(int port) {
            builder.usingPort(port);
            return this;
        }

        /**
         * If enabled it will find any free port to assign to the instance of the server
         */
        public Builder usingAnyFreePort() {
            this.builder.usingAnyFreePort();
            return this;
        }

        public Builder fromBundle(String name, String bundleFile) {
            final EmbeddedHttpGitServerBuilder builder = EmbeddedHttpGitServer.fromBundle(name, bundleFile);
            this.builder.mergeLocations(builder);
            return this;
        }

        public Builder fromBundle(String bundleFile) {
            final EmbeddedHttpGitServerBuilder builder = EmbeddedHttpGitServer.fromBundle(bundleFile, bundleFile);
            this.builder.mergeLocations(builder);
            return this;
        }

        public Builder fromPath(Path path) {
            final EmbeddedHttpGitServerBuilder builder = EmbeddedHttpGitServer.fromPath(path);
            this.builder.mergeLocations(builder);
            return this;
        }

        public Builder fromPath(String name, Path path) {
            final EmbeddedHttpGitServerBuilder builder = EmbeddedHttpGitServer.fromFile(name, path.toFile());
            this.builder.mergeLocations(builder);
            return this;
        }

        public Builder fromFile(File file) {
            final EmbeddedHttpGitServerBuilder
                builder = EmbeddedHttpGitServer.fromFile(extractName(file.getAbsolutePath()), file);
            this.builder.mergeLocations(builder);
            return this;
        }

        public Builder fromFile(String name, File file) {
            final EmbeddedHttpGitServerBuilder builder = EmbeddedHttpGitServer.fromFile(name, file);
            this.builder.mergeLocations(builder);
            return this;
        }

        public Builder fromUrl(URL url) {
            final EmbeddedHttpGitServerBuilder
                builder =  EmbeddedHttpGitServer.fromUrl(extractName(url.toExternalForm()), url);
            this.builder.mergeLocations(builder);
            return this;
        }

        public Builder fromUrl(String name, URL url) {
            final EmbeddedHttpGitServerBuilder builder = EmbeddedHttpGitServer.fromUrl(name, url);
            this.builder.mergeLocations(builder);
            return this;
        }

        public GitServer create() {
            return new GitServer(builder.create());
        }
    }

}
