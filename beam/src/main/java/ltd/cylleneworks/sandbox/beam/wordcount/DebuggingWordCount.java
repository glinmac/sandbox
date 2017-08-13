package ltd.cylleneworks.sandbox.beam.wordcount;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class DebuggingWordCount {

    static interface WordCountOptions extends PipelineOptions {
        @Description("Path of the file to read from")
        @Default.String("*")
        String getInput();

        void setInput(String value);

        @Description("Path of the file to write to")
        @Default.String("output")
        String getOutput();

        void setOutput(String value);

        @Description("Regex filter pattern to filter worder in output")
        @Default.String("Flourish|stomach")
        String getFilterPattern();

        void setFilterPattern(String value);
    }

    public static class ExtractWordsFn extends DoFn<String, String> {

        public final Counter emptyLines = Metrics.counter(ExtractWordsFn.class, "emptyLines");

        @ProcessElement
        public void processElement(ProcessContext c) {
            if (c.element().trim().isEmpty()) {
                emptyLines.inc();
            } else {
                for (String word : c.element().split("[^\\p{L}]+")) {
                    if (!word.isEmpty()) {
                        c.output(word);
                    }
                }
            }
        }
    }

    static class CountWords extends PTransform<PCollection<String>, PCollection<KV<String, Long>>> {
        @Override
        public PCollection<KV<String, Long>> expand(PCollection<String> lines) {
            PCollection<String> words = lines.apply("ExtractWords", ParDo.of(new ExtractWordsFn()));
            PCollection<KV<String, Long>> wordCounts = words.apply("Count", Count.<String>perElement());
            return wordCounts;
        }
    }

    static class FormatCountWords extends SimpleFunction<KV<String, Long>, String> {
        @Override
        public String apply(KV<String, Long> count) {
            return String.format("(%s, %s)", count.getKey(), count.getValue());
        }
    }

    static class FilterFn extends DoFn<KV<String, Long>, KV<String, Long>> {

        static Logger logger = LoggerFactory.getLogger(FilterFn.class);

        private final Pattern filter;

        public FilterFn(String pattern) {
            filter = Pattern.compile(pattern);
        }

        private final Counter matchedWords = Metrics.counter(FilterFn.class, "matched");
        private final Counter unmatchedWords = Metrics.counter(FilterFn.class, "unmatched");

        @ProcessElement
        public void processElement(ProcessContext context) {
            if (filter.matcher(context.element().getKey()).matches()) {
                logger.debug("Matched: " + context.element().getKey());
                matchedWords.inc();
                context.output(context.element());
            } else {
                logger.trace("Did not match: " + context.element().getKey());
                unmatchedWords.inc();
            }
        }

    }

    public static void main(String args[]) {

        WordCountOptions options = PipelineOptionsFactory.fromArgs(args)
            .withValidation()
            .as(WordCountOptions.class);

        Pipeline p = Pipeline.create(options);

        PCollection<KV<String, Long>> filteredWords = p.apply("Read Lines", TextIO.read().from(options.getInput()))
            .apply("CountWords", new CountWords())
            .apply(ParDo.of(new FilterFn(options.getFilterPattern())));

        filteredWords.apply("FormatResults", MapElements.via(new FormatCountWords()))
            .apply("WriteOutput", TextIO.write().to(String.format("%s/wordcounts", options.getOutput())));

        List<KV<String, Long>> expectedResults = Arrays.asList(
            KV.of("Flourish", 3L),
            KV.of("stomach", 1L));
        PAssert.that(filteredWords).containsInAnyOrder(expectedResults);

        p.run().waitUntilFinish();

    }

}
