package org.arquillian.smart.testing.strategies.categorized.custom.assertions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.arquillian.smart.testing.TestSelection;
import org.assertj.core.api.AbstractAssert;

public class TestSelectionCollectionAssert
    extends AbstractAssert<TestSelectionCollectionAssert, Collection<TestSelection>> {

    public TestSelectionCollectionAssert(Collection<TestSelection> testSelectionCollection) {
        super(testSelectionCollection, TestSelectionCollectionAssert.class);
    }

    public static TestSelectionCollectionAssert assertThat(Collection<TestSelection> testSelectionCollection) {
        return new TestSelectionCollectionAssert(testSelectionCollection);
    }

    public void containsTestClassSelectionsExactlyInAnyOrder(TestSelection... expectedTestSelections) {
        isNotNull();
        List<TestSelection> expectedSelections = Arrays.asList(expectedTestSelections);

        List<TestSelection> notFound = actual.stream()
            .filter(actualTestSelection -> !isTestSelectionInTheList(actualTestSelection, expectedSelections))
            .collect(Collectors.toList());

        if (!notFound.isEmpty()) {
            failWithMessage(
                "The actual collection of test selections: \n<%s> \n\nshould contain exactly in any order: \n <%s>, "
                    + "\n\nbut it wasn't possible to find: \n<%s>",
                actual, expectedSelections, notFound);
        }

        if (actual.size() != expectedSelections.size()) {
            List<TestSelection> notExpected = expectedSelections.stream()
                .filter(
                    expectedTestSelection -> !isTestSelectionInTheList(expectedTestSelection, new ArrayList<>(actual)))
                .collect(Collectors.toList());

            failWithMessage(
                "The actual collection of test selections: \n<%s> \n\nshould contain exactly in any order: \n <%s>, "
                    + "\n\nbut these selections are present and not expected: \n<%s>",
                actual, expectedSelections, notExpected);
        }
    }

    private boolean isTestSelectionInTheList(TestSelection testSelection, List<TestSelection> testSelectionList) {
        int index = testSelectionList.indexOf(testSelection);
        if (index >= 0) {
            TestSelection expTestSelection = testSelectionList.get(index);
            if (!collectionsHasExactlySameContent(testSelection.getAppliedStrategies(),
                expTestSelection.getAppliedStrategies())) {
                return false;
            }
            if (!collectionsHasSameContent(testSelection.getTestMethodNames(),
                expTestSelection.getTestMethodNames())) {
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean collectionsHasExactlySameContent(Collection<String> actualCollection,
        Collection<String> expectedCollection) {
        if (!nullAndSizeVerification(actualCollection, expectedCollection)) {
            return false;
        }
        for (int i = 0; i < actualCollection.size(); i++) {
            if (!Objects.equals(actualCollection.toArray()[i], expectedCollection.toArray()[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean collectionsHasSameContent(Collection<String> actualCollection,
        Collection<String> expectedCollection) {
        if (!nullAndSizeVerification(actualCollection, expectedCollection)) {
            return false;
        }
        if (actualCollection.isEmpty()){
            return true;
        }

        return actualCollection.stream().anyMatch(actualContent -> expectedCollection.contains(actualContent));
    }

    private boolean nullAndSizeVerification(Collection<String> actualCollection, Collection<String> expectedCollection) {
        if ((actualCollection == null && expectedCollection != null)
            || (actualCollection != null && expectedCollection == null)) {
            return false;
        }
        if (actualCollection.size() != expectedCollection.size()) {
            return false;
        }
        return true;
    }
}
