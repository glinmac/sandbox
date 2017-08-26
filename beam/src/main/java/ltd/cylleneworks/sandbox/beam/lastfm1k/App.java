package ltd.cylleneworks.sandbox.beam.lastfm1k;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.extensions.joinlibrary.Join;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    static interface AppOptions extends PipelineOptions {
        @Description("Path of the profiles files")
        String getProfile();
        void setProfile(String value);

        @Description("Path of the listens files")
        String getListen();
        void setListen(String value);

        @Description("Output path")
        String getOutput();
        void setOutput(String value);

    }

    public static void main(String args[]) {

        AppOptions options = PipelineOptionsFactory.fromArgs(args)
            .withValidation()
            .as(AppOptions.class);

        Pipeline p = Pipeline.create(options);

        Logger logger = LoggerFactory.getLogger(App.class);

        PCollection<KV<String, Profile>> profiles =
            p.apply("Read from input", TextIO.read().from(options.getProfile()))
            .apply("Parse records", ParDo.of(new DoFn<String, KV<String, Profile>>() {
                @ProcessElement
                public void processElement(ProcessContext c) {
                    Profile p = Profile.Parse(c.element());
                    c.output(KV.<String, Profile>of(p.getUserId(), p));
                }
            }));

        PCollection<KV<String, Listen>> listens =
            p.apply("Read from input", TextIO.read().from(options.getListen()))
            .apply("Parse records", ParDo.of(new DoFn<String, KV<String, Listen>>() {
                @ProcessElement
                public void processElement(ProcessContext c) {
                    Listen l = Listen.Parse(c.element());
                    c.output(KV.<String, Listen>of(l.getUserId(), l));
                }
            }));

        listens.apply("Convert to String",
            MapElements.into(TypeDescriptors.strings())
                .via(input -> input.getValue().toString()))
            .apply("Save to file", TextIO.write().to(options.getOutput() + "/listens"));

        profiles.apply("Convert to String",
            MapElements.into(TypeDescriptors.strings())
                .via(input -> input.getValue().toString()))
            .apply("Save to file", TextIO.write().to(options.getOutput() + "/profiles"));

        // Perform join
        Join.leftOuterJoin(listens, profiles, Profile.NULL)
            .apply("FormatResults",
                MapElements.into(TypeDescriptors.strings())
                    .via(input -> String.format("%s %s %s", input.getKey(), input.getValue().getKey().getArtistName(), input.getValue().getValue().getCountry())))
            .apply("Save to file", TextIO.write().to(options.getOutput() + "/join"));
        p.run().waitUntilFinish();
    }
}
