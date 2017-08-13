package ltd.cylleneworks.sandbox.beam.wordcount;

import com.google.common.io.Files;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.DoFnTester;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.nio.charset.StandardCharsets;

@RunWith(JUnit4.class)
public class DebuggingWordCountTest {

    @Test
    public void testExtractWordsFn() throws Exception {
        DoFnTester<String, String> extractWordsFn = DoFnTester.of(new DebuggingWordCount.ExtractWordsFn());

        Assert.assertThat(
            extractWordsFn.processBundle("one two 3 . 89 test!"),
            CoreMatchers.hasItems("one", "two", "test")
        );

        Assert.assertThat(
            extractWordsFn.processBundle(" "),
            CoreMatchers.<String>hasItems()
        );

        Assert.assertThat(
            extractWordsFn.processBundle(" some ", " input", " words"),
            CoreMatchers.hasItems("some", "input", "words")
        );
    }

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder(new File("data"));

    @Test
    public void testDebuggingWordCount() throws Exception {
        File inputFile = tmpFolder.newFile();
        Files.write("stomach secret Flourish message Flourish here Flourish", inputFile, StandardCharsets.UTF_8);
        DebuggingWordCount.WordCountOptions options = TestPipeline.testingPipelineOptions().as(DebuggingWordCount.WordCountOptions.class);
        options.setInput(inputFile.getAbsolutePath());
        options.setOutput("data/output");
        DebuggingWordCount.main(TestPipeline.convertToArgs(options));
    }
}
