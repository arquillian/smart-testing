package org.arquillian.smart.testing.hub.storage.local;

import java.io.File;

public class LocalStorage {

    private String rootDir;

    public LocalStorage(String rootDir) {
        this.rootDir = rootDir;
    }

    public LocalStorage(File rootDir) {
        this.rootDir = rootDir.getAbsolutePath();
    }

    public DuringExecutionLocalStorage duringExecution() {
        return new DuringExecutionLocalStorage(rootDir);
    }

    public AfterExecutionLocalStorage afterExecution() {
        return new AfterExecutionLocalStorage(rootDir + File.separator + "target");
    }

    public AfterExecutionLocalStorage afterExecution(String pathToCustomTarget) {
        if (pathToCustomTarget == null){
            return afterExecution();
        }
        return new AfterExecutionLocalStorage(pathToCustomTarget);
    }
}
