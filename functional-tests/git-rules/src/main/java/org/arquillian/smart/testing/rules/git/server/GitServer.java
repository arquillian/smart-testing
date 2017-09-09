package org.arquillian.smart.testing.rules.git.server;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import org.junit.rules.ExternalResource;

/**
 *
 * This rule starts a HTTP Git Server serving Git repository imported from other location so it can be used while testing
 * git-related logic.
 *
 * To use it simply add
 *
 * <pre>
 * &#064;Rule
 * public final TestRule gitServer = GitServer.fromBundle("bundle.file").create();
 * </pre>
 *
 * @see EmbeddedHttpGitServer
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

    public static Builder fromBundle(String bundleFile) {
        return new Builder(EmbeddedHttpGitServer.fromBundle(bundleFile));
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

    public static class Builder {

        private final EmbeddedHttpGitServer.Builder builder;

        public Builder(EmbeddedHttpGitServer.Builder builder) {
            this.builder = builder;
        }

        public Builder usingPort(int port) {
            builder.usingPort(port);
            return this;
        }

        public GitServer create() {
            return new GitServer(builder.create());
        }

    }

}