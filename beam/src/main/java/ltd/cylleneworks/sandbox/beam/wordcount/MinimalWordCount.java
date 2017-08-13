package ltd.cylleneworks.sandbox.beam.wordcount;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;

public class MinimalWordCount {
    public static void main(String args[]) {
        String input = args[0];
        String output = args[1];

        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline p = Pipeline.create(options);
        p.apply(TextIO.read().from(input))
            .apply("ExtractWords", ParDo.of(new DoFn<String, String>() {
                @ProcessElement
                public void processElement(ProcessContext c) {
                    for (String word : c.element().split("[^\\p{L}]+")) {
                        if (!word.isEmpty()) {
                            c.output(word);
                        }
                    }
                }
            }))
            .apply(Count.<String>perElement())
            .apply("FormatResults", MapElements.via(new SimpleFunction<KV<String, Long>, String>() {
                @Override
                public String apply(KV<String, Long> input) {
                    return String.format("(%s, %s)", input.getKey(), input.getValue());
                }
            }))
            .apply(TextIO.write().to(String.format("%s/wordcounts", output)));
        p.run().waitUntilFinish();
    }

}
