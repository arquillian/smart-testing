package org.arquillian.smart.testing.vcs.git;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.spi.TestExecutionPlanner;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

public class NewFilesDetector implements TestExecutionPlanner {

    private final File repoRoot;
    private final String previous;
    private final String head;

    public NewFilesDetector(File repoRoot, String previous, String head) {
        this.previous = previous;
        this.head = head;
        this.repoRoot = repoRoot;
    }

    @Override
    public Iterable<String> getTests() {
        final FileRepositoryBuilder builder = new FileRepositoryBuilder();

        try (Repository repository = builder.readEnvironment().findGitDir(repoRoot).build();
             ObjectReader reader = repository.newObjectReader();
             Git git = new Git(repository)) {

            final ObjectId oldHead = repository.resolve(this.previous + "^{tree}");
            final ObjectId head = repository.resolve(this.head + "^{tree}");

            final CanonicalTreeParser oldTree = new CanonicalTreeParser();
            oldTree.reset(reader, oldHead);
            final CanonicalTreeParser newTree = new CanonicalTreeParser();
            newTree.reset(reader, head);

            final List<DiffEntry> diffs = git.diff().setNewTree(newTree).setOldTree(oldTree).call();
            final String repoRoot = repository.getDirectory().getParent();

            return diffs.stream()
                .filter(diffEntry -> DiffEntry.ChangeType.ADD == diffEntry.getChangeType())
                .map(diffEntry -> {
                    try {
                        final File sourceFile = new File(repoRoot, diffEntry.getNewPath());
                        return extractFullyQualifiedName(sourceFile);
                    } catch (FileNotFoundException e) {
                        throw new IllegalArgumentException(e);
                    }
                })
                .collect(Collectors.toList());
        } catch (IOException | GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    private String extractFullyQualifiedName(File sourceFile) throws FileNotFoundException {
        final CompilationUnit compilationUnit= JavaParser.parse(sourceFile);
        final Optional<ClassOrInterfaceDeclaration> newClass =
            compilationUnit.getClassByName(sourceFile.getName().replaceAll(".java", ""));
        return compilationUnit.getPackageDeclaration().get().getNameAsString() + "." + newClass.get().getNameAsString();
    }
}
