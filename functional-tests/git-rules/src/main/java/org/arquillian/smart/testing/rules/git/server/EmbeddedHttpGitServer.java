package org.arquillian.smart.testing.rules.git.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;

import static java.util.Optional.ofNullable;
import static org.arquillian.smart.testing.rules.git.server.UrlNameExtractor.extractName;

/**
 * Embedded web server hosting single git repository. As a source you can point to a bundle file (see git-bundle), other git repository or
 * local file system to clone.
 *
 * This will get the content of remote repository, clone into to temporary folder (e.g. /tmp/git-cloned-repo-8011663866191655452-repo-name) and
 * expose this repository over HTTP, so you can call
 *
 * git clone http://localhost:8765/repo-name
 *
 * and can interact with it locally.
 *
 * 8765 is a default port. This can changed either by passing extra constructor argument, or setting system property git.server.port
 *
 * IMPORTANT: unless initializeAll is explicitly called, all repositories are lazily cloned upon the first external call
 * IMPORTANT: ignores nested URLs, meaning http://localhost:8765/repo-name == http://localhost:8765/org1/repo-name == http://localhost:8765/org2/repo-name.git
 *
 * Code inspired by https://github.com/centic9/jgit-cookbook/tree/master/httpserver
 */
public class EmbeddedHttpGitServer {

    private static final Logger LOGGER = Logger.getLogger(EmbeddedHttpGitServer.class.getName());
    private static final String SUFFIX = ".git";

    private final int port;
    private final Map<String, LazilyLoadedRepository> repositories = new HashMap<>();

    private Server server;

    EmbeddedHttpGitServer(Map<String, String> repositoryLocations, int port, boolean useAvailablePort) {
        if (useAvailablePort) {
            this.port = getAvailableLocalPort();
        } else {
            this.port = port;
        }
        repositoryLocations.forEach((name, location) -> repositories.put(name, new LazilyLoadedRepository(name, location)));
    }

    public void start() throws Exception {
        this.server = createGitServer(port);
    }

    public void stop() {
        stopServer();
        cleanupResources();
    }

    public int getPort() {
        return port;
    }

    /**
     * Eagerly clones all specified repositories
     */
    public void initializeAll() {
        repositories.values().forEach(LazilyLoadedRepository::cloneRepository);
    }

    private void stopServer() {
        try {
            if (this.server != null) {
                this.server.stop();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed while stopping server", e);
        }
    }

    private void cleanupResources() {
        repositories.values().forEach(LazilyLoadedRepository::close);
    }

    private Server createGitServer(int port) throws Exception {
        final Server server = new Server(port);

        final ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(new ServletHolder(createGitServlet()), "/*");

        server.start();
        server.setStopAtShutdown(true);

        LOGGER.info("Started serving local git repositories [" + this.repositories.values().stream().map(
            LazilyLoadedRepository::getName).collect(Collectors.toList()) + "] under http://localhost:" + port);
        return server;
    }

    private GitServlet createGitServlet() {
        final GitServlet gitServlet = new GitServlet();
        gitServlet.setRepositoryResolver((req, name) -> {
            String trimmedName = name.endsWith(SUFFIX) ? name.substring(0, name.length() - SUFFIX.length()) : name;
            trimmedName = trimmedName.substring(trimmedName.lastIndexOf('/') + 1);
            if (repositories.containsKey(trimmedName)) {
                final LazilyLoadedRepository lazilyLoadedRepository = repositories.get(trimmedName);
                synchronized (gitServlet) {
                    lazilyLoadedRepository.cloneRepository();
                    final Repository repository = lazilyLoadedRepository.get();
                    enableInsecureReceiving(repository);
                    repository.incrementOpen();
                    return repository;
                }
            } else {
                throw new RepositoryNotFoundException("Repository " + name + "does not exist");
            }
        });
        gitServlet.addReceivePackFilter(new AfterReceivePackResetFilter(repositories.values()));
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

    static int definedPort() {
        return Integer.valueOf(System.getProperty("git.server.port", "8765"));
    }

    /**
     * Creates {@link EmbeddedHttpGitServer} serving repository imported from a bundle file located on the class path.
     * See <a href="https://git-scm.com/docs/git-bundle">git-bundle</a> documentation for details how to create bundle file.
     *
     * @param name under which the repository will be served
     * @param bundleFile bundle file name. should be present on the classpath
     */
    public static EmbeddedHttpGitServerBuilder fromBundle(String name, String bundleFile) {
        final URL bundleResource = loadBundle(".", bundleFile);
        return new EmbeddedHttpGitServerBuilder(name, bundleResource.toExternalForm());
    }

    /**
     * Creates {@link EmbeddedHttpGitServer} serving repository imported from a bundle file located on the class path.
     * See <a href="https://git-scm.com/docs/git-bundle">git-bundle</a> documentation for details how to create bundle file.
     *
     * Takes bundle name as a name under which the repository will be served
     *
     * @param bundleFile bundle file name. should be present on the classpath
     */
    public static EmbeddedHttpGitServerBuilder fromBundle(String bundleFile) {
        return fromBundle(bundleFile, bundleFile);
    }

    /**
     * Creates {@link EmbeddedHttpGitServer} serving repository imported from any other repository (or bundle file) available on
     * the file system.
     *
     * Takes last part of the path as a name under which the repository will be served
     *
     * @param path Path to git repository or bundle file.
     */
    public static EmbeddedHttpGitServerBuilder fromPath(Path path) {
        return fromFile(path.toFile());
    }

    /**
     * Creates {@link EmbeddedHttpGitServer} serving repository imported from any other repository (or bundle file) available on
     * the file system.
     *
     * Takes last part of the path as a name under which the repository will be served
     *
     * @param path Path to git repository or bundle file.
     */
    public static EmbeddedHttpGitServerBuilder fromPath(String name, Path path) {
        return fromFile(name, path.toFile());
    }

    /**
     * Creates {@link EmbeddedHttpGitServer} serving repository imported from any other repository (or bundle file) available on
     * the file system.
     *
     * Takes last part of the path as a name under which the repository will be served
     *
     * @param file Path to git repository or bundle file.
     */
    public static EmbeddedHttpGitServerBuilder fromFile(File file) {
        return fromFile(extractName(file.getAbsolutePath()), file);
    }

    /**
     * Creates {@link EmbeddedHttpGitServer} serving repository imported from any other repository (or bundle file) available on
     * the file system.
     *
     * Takes last part of the path as a name under which the repository will be served
     *
     * @param file Path to git repository or bundle file.
     */
    public static EmbeddedHttpGitServerBuilder fromFile(String name, File file) {
        return new EmbeddedHttpGitServerBuilder(name, file.getAbsolutePath());
    }

    /**
     * Creates {@link EmbeddedHttpGitServer} serving repository imported from remote repository
     * the file system.
     *
     * Takes last part of the URL (without .git extension) as a name under which the repository will be served
     *
     * @param url url of remote repository
     */
    public static EmbeddedHttpGitServerBuilder fromUrl(URL url) {
        return fromUrl(extractName(url.toExternalForm()), url);
    }

    /**
     * Creates {@link EmbeddedHttpGitServer} serving repository imported from remote repository
     * the file system.
     *
     * @param name under which the repository will be served
     * @param url url of remote repository
     */
    public static EmbeddedHttpGitServerBuilder fromUrl(String name, URL url) {
        return new EmbeddedHttpGitServerBuilder(name, url.toExternalForm());
    }

    /**
     * Scans directory for *.bundle files and creates repositories named after a file without .bundle extension
     * @param bundleDirectory directory to lookup for *.bundle files
     * @return
     */
    public static EmbeddedHttpGitServerBuilder bundlesFromDirectory(String bundleDirectory) {
        final URL bundleDirUrl = ((URLClassLoader) Thread.currentThread().getContextClassLoader()).findResource(bundleDirectory);
        if (bundleDirUrl == null) {
            throw new IllegalStateException("Unable to find bundle directory " + bundleDirectory);
        }
        final File bundleDir = new File(bundleDirUrl.getFile());
        return Arrays.stream(ofNullable(bundleDir.listFiles()).orElse(new File[0]))
            .filter(file -> file.getName().endsWith(".bundle"))
            .map(file -> fromFile(file.getName().substring(0, file.getName().lastIndexOf(".bundle")), file))
            .reduce(EmbeddedHttpGitServerBuilder::mergeLocations)
            .orElseThrow(() -> new IllegalArgumentException("Directory [" + bundleDirectory + "] doesn't contain .bundle files"));
    }

    private static URL loadBundle(String directory, String bundleFile) {
        final URL bundleResource = Thread.currentThread().getContextClassLoader().getResource(directory + "/" + bundleFile);
        if (bundleResource == null) {
            throw new IllegalArgumentException(bundleFile + " couldn't be found on the classpath");
        }
        return bundleResource;
    }

    private int getAvailableLocalPort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed obtaining free port", e);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // ouch
                    e.printStackTrace();
                }
            }
        }
    }
}
