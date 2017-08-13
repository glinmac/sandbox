package ltd.cylleneworks.sandbox.beam.wordcount;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

public class WordCount {

    static interface WordCountOptions extends PipelineOptions {
        @Description("Path of the file to read from")
        @Default.String("*")
        String getInput();
        void setInput(String value);

        @Description("Path of the file to write to")
        @Default.String("output")
        String getOutput();
        void setOutput(String value);
    }

    static class ExtractWordsFn extends DoFn<String, String> {

        private final Counter emptyLines = Metrics.counter(ExtractWordsFn.class, "emptyLines");

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

    public static void main(String args[]) {

        WordCountOptions options = PipelineOptionsFactory.fromArgs(args)
            .withValidation()
            .as(WordCountOptions.class);

        Pipeline p = Pipeline.create(options);
        p.apply(TextIO.read().from(options.getInput()))
            .apply("CountWords", new CountWords())
            .apply("FormatResults", MapElements.via(new FormatCountWords()))
            .apply("WriteOutput", TextIO.write().to(String.format("%s/wordcounts", options.getOutput())));
        p.run().waitUntilFinish();
    }

}
