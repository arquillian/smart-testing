package org.arquillian.smart.testing.scm;

import java.util.Arrays;
import org.eclipse.jgit.diff.DiffEntry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class ChangeTypeConsistencyTest {

    @Parameterized.Parameters
    public static Iterable<DiffEntry.ChangeType> jgitChangeTypes() {
        return Arrays.asList(DiffEntry.ChangeType.values());
    }

    private final DiffEntry.ChangeType diffEntryChangeType;

    public ChangeTypeConsistencyTest(DiffEntry.ChangeType diffEntryChangeType) {
        this.diffEntryChangeType = diffEntryChangeType;
    }

    @Test
    public void should_handle_all_jgit_change_types() throws Exception {
        assertThat(ChangeType.valueOf(diffEntryChangeType.name())).isNotNull();
    }

}
