package ltd.cylleneworks.sandbox.beam.wordcount;

import org.apache.beam.examples.common.WriteOneFilePerWindow;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.DefaultFilenamePolicy;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.fs.ResourceId;
import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import java.util.concurrent.ThreadLocalRandom;

import org.apache.beam.sdk.values.PDone;
import org.joda.time.Duration;
import org.joda.time.Instant;

public class WindowedWordCount {

    final static int WINDOW_SIZE = 1;

    static interface WindowedWordCountOptions extends PipelineOptions {
        @Description("Path of the file to read from")
        @Default.String("*")
        String getInput();
        void setInput(String value);

        @Description("Path of the file to write to")
        @Default.String("output")
        String getOutput();
        void setOutput(String value);

        @Description("Minimum randomly assigned timestamp, in seconds-since-eopch")
        @Default.InstanceFactory(DefaultToCurrentSystemTime.class)
        Long getMinTs();
        void setMinTs(Long value);

        @Description("Maximum randomly assigned timestamp, in seconds-since-epoch")
        @Default.InstanceFactory(DefaultToMinTimestampPlusOneHour.class)
        Long getMaxTs();
        void setMaxTs(Long value);

        @Description("Fixed window duration, in minutes")
        @Default.Integer(WINDOW_SIZE)
        Integer getWindowSize();
        void setWindowSize(Integer value);

        @Description("Num shards per window")
        Integer getNumShards();
        void setNumShards(Integer numShards);
    }

    public static class DefaultToCurrentSystemTime implements DefaultValueFactory<Long> {
        @Override
        public Long create(PipelineOptions options) {
            return System.currentTimeMillis()/1000;
        }
    }

    public static class DefaultToMinTimestampPlusOneHour implements DefaultValueFactory<Long> {
        @Override
        public Long create(PipelineOptions options) {
            return options.as(WindowedWordCountOptions.class).getMinTs()
                + Duration.standardHours(1).getMillis()/1000;
        }
    }
    static class AddTsFn extends DoFn<String, String> {
        private final Instant minTs;
        private final Instant maxTs;

        AddTsFn(Instant minTs, Instant maxTs) {
            this.minTs = minTs;
            this.maxTs = maxTs;
        }

        @ProcessElement
        public void processElement(ProcessContext context) {
            Instant rndTs =
                new Instant(ThreadLocalRandom.current()
                    .nextLong(minTs.getMillis()/1000, maxTs.getMillis()/1000));
            context.outputWithTimestamp(context.element(), new Instant(rndTs));
        }
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

        WindowedWordCountOptions options = PipelineOptionsFactory
            .fromArgs(args)
            .withValidation()
            .as(WindowedWordCountOptions.class);

        Instant minTs = new Instant(options.getMinTs());
        Instant maxTs = new Instant(options.getMaxTs());

        Pipeline p = Pipeline.create(options);

        p.apply(TextIO.read().from(options.getInput()))
            .apply("TimeStamp",
                ParDo.of(new AddTsFn(minTs, maxTs)))
            .apply("Windowing",
                Window.<String>into(FixedWindows.of(Duration.standardMinutes(options.getWindowSize()))))
            .apply("CountWords", new CountWords())
            .apply("FormatResults", MapElements.via(new FormatCountWords()))
            .apply("WriteOutput",
                new WriteOneFilePerWindow("%s/wordcount".format(options.getOutput()), options.getNumShards()));

        p.run().waitUntilFinish();
    }

}
