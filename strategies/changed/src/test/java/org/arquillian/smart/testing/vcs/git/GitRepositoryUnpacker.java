package org.arquillian.smart.testing.vcs.git;

import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.task.os.CommandTool;

class GitRepositoryUnpacker {

    static void unpackRepository(String repoTarget, String repoBundleFile) {
        Spacelift.task(CommandTool.class)
            .command(new CommandBuilder("git")
                .parameters("clone", repoBundleFile, "-b", "master",
                    repoTarget).build())
            .execute().await();
    }
}
