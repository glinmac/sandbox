package ltd.cylleneworks.sandbox.beam.twitter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.extensions.jackson.AsJsons;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;

import java.util.ArrayList;


public class Projection {

    public static void main(String args[]) {

        ProjectionOptions options = PipelineOptionsFactory.fromArgs(args)
            .withValidation()
            .as(ProjectionOptions.class);

        Pipeline p = Pipeline.create(options);

        p.apply("Read from input", TextIO.read().from(options.getTwitterData()))
            .apply("Parse Input And Project", ParDo.of(new ProjectTweetFn()))
            .apply("Convert to Jsons", AsJsons.of(TweetData.class))
            .apply("Write to output", TextIO.write().to(String.format("%s/projection", options.getOutput())));

        p.run().waitUntilFinish();
    }

    // Options
    interface ProjectionOptions extends PipelineOptions {
        @Description("Path of the twitter data")
        String getTwitterData();

        void setTwitterData(String value);

        @Description("Output path")
        String getOutput();

        void setOutput(String value);

    }

    // Only select a few given properties of a tweet
    static class ProjectTweetFn extends DoFn<String, TweetData> {

        Counter failures = Metrics.counter(ProjectTweetFn.class, "failures");

        @ProcessElement
        public void processElement(ProcessContext c) {

            try {

                JsonObject data = new JsonParser().parse(c.element()).getAsJsonObject();
                TweetData d = new TweetData();

                d.id = data.get("id").getAsString();
                d.created_at = data.get("created_at").getAsString();
                d.text = data.get("text").getAsString();
                d.lang = data.get("lang").getAsString();
                d.hashtags = new ArrayList<>();

                for (JsonElement el : data.get("entities").getAsJsonObject().get("hashtags").getAsJsonArray()) {
                    d.hashtags.add(el.getAsJsonObject().get("text").getAsString());
                }

                c.output(d);

            } catch (Exception ex) {
                failures.inc();
            }
        }
    }
}
