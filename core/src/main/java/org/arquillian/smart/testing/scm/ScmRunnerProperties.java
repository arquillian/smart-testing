package org.arquillian.smart.testing.scm;

public class ScmRunnerProperties {

    public static final String PREVIOUS_COMMIT = "scm.range.tail";
    public static final String COMMIT = "scm.range.head";
    public static final String LAST_COMMITS = "scm.last.changes";
    public static final String HEAD = "HEAD";


    public static String getPrevCommitDefaultValue() {
        return HEAD + "~" + System.getProperty(LAST_COMMITS, "0");
    }
}

