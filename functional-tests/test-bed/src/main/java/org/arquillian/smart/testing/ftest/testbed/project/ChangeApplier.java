package org.arquillian.smart.testing.ftest.testbed.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResultsExtractor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;

import static java.util.Arrays.asList;
import static org.eclipse.jgit.api.ResetCommand.ResetType.SOFT;

class ChangeApplier {

    private static final String ONE_BACK = "HEAD~";

    private final Repository repository;
    private final Git git;
    private final TestResultsExtractor testResultsExtractor;

    ChangeApplier(Repository repository) {
        this.repository = repository;
        this.git = new Git(repository);
        this.testResultsExtractor = new TestResultsExtractor(repository);
    }

    /**
     *
     * Applies changes as commits on top of the current branch
     *
     * @param changeDescriptions list of messages associated with the tags
     * @return list of expected test results which are specified in the commit messages tagged by changeDescriptions.
     *
     * @see TestResultsExtractor#expectedTestResults(ObjectId)
     */
    Collection<TestResult> applyAsCommits(String... changeDescriptions) {
       return apply(this::cherryPickTags, changeDescriptions);
    }

    /**
     * Applies changes locally without committing to the local repository.
     *
     * Each description should correlate with the message of a tag
     *
     * @param changeDescriptions list of messages associated with the tags
     * @return list of expected test results which are specified in the commit messages tagged by changeDescriptions
     *
     * @see TestResultsExtractor#expectedTestResults(ObjectId)
     */
    Collection<TestResult> applyLocally(String... changeDescriptions) {
        return apply(this::applyLocallyFromTags, changeDescriptions);
    }

    Collection<TestResult> apply(ChangeApplicator applicator, String ... changeDescriptions) {
        try {
            final Collection<RevTag> matchingTags = findMatchingTags(changeDescriptions);
            if (changeDescriptions.length != matchingTags.size()) {
                throw new IllegalStateException("Unable to find all required changes " + Arrays.toString(changeDescriptions)
                    + ", found only " + matchingTags.stream().map(tag -> tag.getTagName() + ": " + tag.getFullMessage()).collect(Collectors.toList()));
            }
            return applicator.apply(matchingTags);
        } catch (GitAPIException e) {
            throw new RuntimeException("Failed applying changes '" + Arrays.toString(changeDescriptions) + "'", e);
        }
    }

    interface ChangeApplicator {
        Collection<TestResult> apply(Collection<RevTag> tags) throws GitAPIException;
    }

    private Collection<TestResult> cherryPickTags(Collection<RevTag> tags) throws GitAPIException {
        final Set<TestResult> combinedTestResults = new HashSet<>();
        for (final RevTag tag : tags) {
            final ObjectId tagId = tag.getObject().getId();
            git.cherryPick().include(tagId).call();
            combinedTestResults.addAll(testResultsExtractor.expectedTestResults(tagId));
        }
        return combinedTestResults;
    }

    private Collection<TestResult> applyLocallyFromTags(Collection<RevTag> tags) throws GitAPIException {
        final Set<TestResult> combinedTestResults = new HashSet<>();
        final List<RevCommit> stashesToApply = new ArrayList<>();
        stashesToApply.add(git.stashCreate().setIncludeUntracked(true).call());
        for (final RevTag tag : tags) {
            final ObjectId tagId = tag.getObject().getId();
            git.cherryPick().setNoCommit(true).include(tagId).call();
            stashesToApply.add(git.stashCreate().setIncludeUntracked(true).call());
            combinedTestResults.addAll(testResultsExtractor.expectedTestResults(tagId));
        }
        applyStashChangesLocally(stashesToApply);
        return combinedTestResults;
    }

    private void applyStashChangesLocally(List<RevCommit> stashesToApply) throws GitAPIException {
        RevCommit tmpCommit = null;
        // We cannot just apply changes from stash one after the other
        // as git will complain about uncommitted changes when trying
        // to apply second consecutive stash.
        // So we create temporary commits to overcome this issue
        // and reset softly on the way to have it all as local changes
        for (final RevCommit stash : stashesToApply) {
            if (stash == null){
                continue;
            }
            git.stashApply().setStashRef(stash.getName()).call();
            if (tmpCommit != null) {
                git.reset().setRef(ONE_BACK).setMode(SOFT).call();
            }
            tmpCommit = createTemporaryCommit();
        }
        if (tmpCommit != null) {
            git.reset().setRef(ONE_BACK).setMode(SOFT).call();
        }
        git.stashDrop().setAll(true).call();
    }

    private RevCommit createTemporaryCommit() throws GitAPIException {
        git.add().addFilepattern("*").call();
        return git.commit().setMessage("[tmp] workaround for multiple stashes").call();
    }

    private Collection<RevTag> findMatchingTags(String[] changeDescriptions) throws GitAPIException {
        final Set<String> changes = new HashSet<>(asList(changeDescriptions));
        final RevWalk revWalk = new RevWalk(repository);
        final List<RevTag> matchingTags = git.tagList().call().stream().map(ref -> {
            try {
                return revWalk.parseTag(ref.getObjectId());
            } catch (IOException e) {
                throw new RuntimeException("Unable to find tag for " + ref.getObjectId(), e);
            }
        }).filter(revTag -> changes.contains(revTag.getFullMessage().trim()))
            .collect(Collectors.toList());
        revWalk.dispose();
        return matchingTags;
    }
}
