package org.arquillian.smart.testing.ftest.testbed.rules;

import java.io.File;
import org.junit.rules.TemporaryFolder;

import static java.lang.System.getProperty;

public class PersistTempFolder extends TemporaryFolder {

    public PersistTempFolder(File target) {
        super(target);
    }

    @Override
    protected void after() {
        if (!isPersistFolderEnabled()) {
            super.delete();
        }
    }

    private boolean isPersistFolderEnabled() {
        return Boolean.valueOf(getProperty("test.bed.project.persist", Boolean.toString(false)));
    }
}
