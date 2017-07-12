package org.arquillian.smart.testing.scm;

public class GitRunnerProperties {

    public static final String PREVIOUS_COMMIT = "git.previous.commit";
    public static final String COMMIT = "git.commit";
    static final String LAST_COMMITS = "git.last.commits";
    public static final String HEAD = "HEAD";


    public static String getPrevCommitDefaultValue() {
        return HEAD + "~" + System.getProperty(LAST_COMMITS, "0");
    }
}

