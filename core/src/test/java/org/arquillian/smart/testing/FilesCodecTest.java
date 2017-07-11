package org.arquillian.smart.testing;

import java.io.File;
import java.io.IOException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FilesCodecTest {

    @Test
    public void should_compute_sha1_of_file() throws IOException {

        // given
        File testFile = new File("src/test/resources/shatestfile.txt");

        // when
        String sha1 = FilesCodec.sha1(testFile);

        // then
        assertThat(sha1).isEqualToIgnoringCase("b163e0a821f0c11da6a436202ddaf60b73aac533");

    }

}
