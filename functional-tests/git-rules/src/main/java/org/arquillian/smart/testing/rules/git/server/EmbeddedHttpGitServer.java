package org.arquillian.smart.testing.rules.git.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.logging.Logger;
import org.arquillian.smart.testing.rules.git.GitCloner;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;

/**
 * As an origin you can point to a bundle file (see git-bundle), other git repository or local file system to clone
 *
 *
 * This will get the content of the bundle.file and clone into to temporary folder (e.g. /tmp/git-cloned-repo-8011663866191655452-repo-name) and
 * expose this repository over HTTP, so when calling
 *
 * git clone http://localhost:8765
 *
 * you will clone the repository served by it.
 *
 * 8765 is a default port. This can changed either by passing extra constructor argument, or setting system property git.server.port
 *
 * Current limitations
 *   - repository name after the name doesn't matter, it's always only one per given server
 *
 * Code inspired by https://github.com/centic9/jgit-cookbook/tree/master/httpserver
 */
public class EmbeddedHttpGitServer {

    private static final Logger LOGGER = Logger.getLogger(EmbeddedHttpGitServer.class.getName());

    private final int port;
    private final GitCloner gitCloner;

    private Server server;
    private Repository repository;

    private EmbeddedHttpGitServer(String gitRepositoryLocation, int port) {
        this.port = port;
        this.gitCloner = new GitCloner(gitRepositoryLocation);
    }

    /**
     * Creates {@link EmbeddedHttpGitServer} serving repository imported from a bundle file located on the class path.
     * See <a href="https://git-scm.com/docs/git-bundle">git-bundle</a> documentation for details how to create bundle file.
     * @param bundleFile bundle file name. should be present on the classpath
     */
    public static Builder fromBundle(String bundleFile) {
        final URL bundleResource = Thread.currentThread().getContextClassLoader().getResource(bundleFile);
        if (bundleResource == null) {
            throw new IllegalArgumentException(bundleFile + " couldn't be found on the classpath");
        }
        return new Builder(bundleResource.toExternalForm());
    }

    /**
     * Creates {@link EmbeddedHttpGitServer} serving repository imported from any other repository (or bundle file) available on
     * the file system.
     * @param path Path to git repository or bundle file.
     */
    public static Builder fromPath(Path path) {
        return fromFile(path.toFile());
    }

    /**
     * Creates {@link EmbeddedHttpGitServer} serving repository imported from any other repository (or bundle file) available on
     * the file system.
     * @param file Path to git repository or bundle file.
     */
    public static Builder fromFile(File file) {
        return new Builder(file.getAbsolutePath());
    }

    /**
     * Creates {@link EmbeddedHttpGitServer} serving repository imported from remote repository
     * the file system.
     * @param url url of remote repository
     */
    public static Builder fromUrl(URL url) {
        return new Builder(url.toExternalForm());
    }


    public void start() throws Exception {
        this.repository = gitCloner.cloneRepositoryToTempFolder();
        this.server = createGitServer(repository, port);
    }

    public void stop() {
        stopServer();
        cleanupResources();
    }

    private void stopServer() {
        try {
            this.server.stop();
        } catch (Exception e) {
            throw new RuntimeException("Failed while stopping server", e);
        }
    }

    private void cleanupResources() {
        this.repository.close();
        gitCloner.removeClone();
    }

    private Server createGitServer(Repository repository, int port) throws Exception {
        final Server server = new Server(port);
        final ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(new ServletHolder(createGitServlet(repository)), "/*");

        server.start();
        server.setStopAtShutdown(true);

        LOGGER.info("Started serving local git repository [" + this.repository.getDirectory() + "] under http://localhost:" + port);
        return server;
    }

    private GitServlet createGitServlet(Repository repository) {
        final GitServlet gitServlet = new GitServlet();
        enableInsecureReceiving(repository);
        gitServlet.setRepositoryResolver((req, name) -> {
            repository.incrementOpen();
            return repository;
        });
        gitServlet.addReceivePackFilter(new AfterReceivePackResetFilter(repository));
        return gitServlet;
    }

    /**
     * To allow performing push operations from the cloned repository to remote (served by this server) let's
     * skip authorization for HTTP.
     */
    private void enableInsecureReceiving(Repository repository) {
        final StoredConfig config = repository.getConfig();
        config.setBoolean("http", null, "receivepack", true);
        try {
            config.save();
        } catch (IOException e) {
            throw new RuntimeException("Unable to save http.receivepack=true config", e);
        }
    }

    private static int definedPort() {
        return Integer.valueOf(System.getProperty("git.server.port", "8765"));
    }

    public static class Builder {

        private final String location;
        private int port = definedPort();

        public Builder(String location) {
            this.location = location;
        }

        public Builder usingPort(int port) {
            this.port = port;
            return this;
        }

        public EmbeddedHttpGitServer create() {
            return new EmbeddedHttpGitServer(location, port);
        }

    }
}
