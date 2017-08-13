package ltd.cylleneworks.sandbox.beam.wordcount;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import com.google.common.io.Files;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.ValidatesRunner;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class WordCountTest {

    @Rule
    public TestPipeline p = TestPipeline.create();

    @Test
    public void testExtractWordsFn() throws Exception {
        DoFnTester<String, String> extractWordsFn = DoFnTester.of(new WordCount.ExtractWordsFn());

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

    @Test
    public void testFormatCountWords() throws Exception {
        SimpleFunction<KV<String, Long>, String> fn = new WordCount.FormatCountWords();
        Assert.assertEquals(
            fn.apply(KV.<String, Long>of("hello", 1L)),
            "(hello, 1)"
        );
    }

    @Test
    @Category(ValidatesRunner.class)
    public void testCountWords() throws Exception {

        List<String> words = Arrays.asList(new String[]{
            "hi there", "hi", "hi sue bob", "hi sue", "", "bob hi"
        });

        PCollection<KV<String, Long>> output = p
            .apply(Create.of(words).withCoder(StringUtf8Coder.of()))
            .apply(new WordCount.CountWords());

        PAssert.that(output)
            .containsInAnyOrder(
                KV.<String, Long>of("hi", 5L),
                KV.<String, Long>of("there", 1L),
                KV.<String, Long>of("sue", 2L),
                KV.<String, Long>of("bob", 2L));

        p.run().waitUntilFinish();
    }

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder(new File("data"));

    @Test
    public void testWordCount() throws Exception {

        File inputFile = tmpFolder.newFile();
        Files.write("stomach secret Flourish message Flourish here Flourish", inputFile, StandardCharsets.UTF_8);

        WordCount.main(new String[]{
            String.format("--input=%s/*", inputFile.getParentFile().getAbsolutePath()),
            "--output=data/output"
        });
    }
}
