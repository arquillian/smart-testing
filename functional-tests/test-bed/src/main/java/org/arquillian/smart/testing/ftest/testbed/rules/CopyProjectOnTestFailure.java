package org.arquillian.smart.testing.ftest.testbed.rules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import static java.util.stream.Collectors.toList;

public class CopyProjectOnTestFailure extends TestWatcher {

    TestBed testBed;

    public CopyProjectOnTestFailure(TestBed testBed) {
        this.testBed = testBed;
    }

    @Override
    protected void failed(Throwable e, Description description) {
        final String targetRepoPerTestFolder = testBed.getTargetRepoPerTestFolder();
        String path = "target" + File.separator + "projects";
        final File projectDir = new File(path);
        projectDir.mkdir();
        if (targetRepoPerTestFolder != null) {
            final Path source = Paths.get(targetRepoPerTestFolder);
            final Path target =
                Paths.get(path + targetRepoPerTestFolder.substring(targetRepoPerTestFolder.lastIndexOf("/")));
            try {
                final List<Path> sources = Files.walk(source).collect(toList());
                final List<Path> targets = sources.stream().map(source::relativize).map(target::resolve)
                    .collect(toList());
                for (int i = 0; i < sources.size(); i++) {
                    Files.copy(sources.get(i), targets.get(i));
                }
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            System.out.println("copied test repository for failed test in: " + target);
        }
    }
}
