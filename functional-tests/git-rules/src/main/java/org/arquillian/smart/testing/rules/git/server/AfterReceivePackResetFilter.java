package org.arquillian.smart.testing.rules.git.server;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import static org.eclipse.jgit.api.ResetCommand.ResetType.HARD;

/**
 *  Simple filter with performs hard reset (git reset --hard) on the cloned repository served by {@link EmbeddedHttpGitServer}
 *  to ensure that working directory is always in the clean state (e.g. after pushes done from clones).
 */
class AfterReceivePackResetFilter implements Filter {

    private final Repository repository;

    AfterReceivePackResetFilter(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // noop
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        chain.doFilter(request, response);
        try {
            Git.wrap(repository).reset().setMode(HARD).call();
        } catch (GitAPIException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void destroy() {
        // noop
    }
}
