package ltd.cylleneworks.sandbox.beam.wordcount;

import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.nio.charset.StandardCharsets;

@RunWith(JUnit4.class)
public class MinimalWordCountTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder(new File("data"));

    @Test
    public void testMinimalWordCount() throws Exception {

        File inputFile = tmpFolder.newFile();
        Files.write("stomach secret Flourish message Flourish here Flourish", inputFile, StandardCharsets.UTF_8);

        MinimalWordCount.main(new String[]{
            String.format("%s/*", inputFile.getParentFile().getAbsolutePath()),
            "data/output"
        });
    }
}
