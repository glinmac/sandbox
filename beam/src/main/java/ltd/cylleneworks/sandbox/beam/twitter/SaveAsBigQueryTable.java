package ltd.cylleneworks.sandbox.beam.twitter;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class SaveAsBigQueryTable {

    public static void main(String args[]) {

        SaveAsBigQueryTableOptions options = PipelineOptionsFactory.fromArgs(args)
            .withValidation()
            .as(SaveAsBigQueryTableOptions.class);

        Pipeline p = Pipeline.create(options);

        TableSchema tableSchema = new TableSchema();
        List<TableFieldSchema> fields = new ArrayList<>();
        fields.add(new TableFieldSchema().setName("id").setType("INT64"));
        fields.add(new TableFieldSchema().setName("ts").setType("TIMESTAMP"));
        fields.add(new TableFieldSchema().setName("text").setType("STRING"));
        fields.add(new TableFieldSchema().setName("lang").setType("STRING"));
        fields.add(new TableFieldSchema().setName("hashtags").setType("STRING").setMode("REPEATED"));
        tableSchema.setFields(fields);

        Counter parseFailures = Metrics.counter(SaveAsBigQueryTable.class, "unmatched");

        Logger logger = LoggerFactory.getLogger(SaveAsBigQueryTable.class);

        TableReference tableRef = new TableReference()
            .setProjectId(options.getProjectId())
            .setDatasetId(options.getDataSetId())
            .setTableId(options.getTableId());


        p
            .apply("Read from input", TextIO.read().from(options.getTwitterData()))
            .apply("Parse Json", ParDo.of(new Projection.ProjectTweetFn()))
            .apply("ToTableRow", ParDo.of(new DoFn<TweetData, TableRow>() {
                @ProcessElement
                public void processElement(ProcessContext c) {
                    TweetData d = c.element();
                    TableRow row = new TableRow();
                    row.set("id", d.id);
                    row.set("text", d.text);
                    row.set("lang", d.lang);
                    row.set("ts", d.timestamp);
                    row.set("hashtags", d.hashtags);
                    c.output(row);
                }
            }))
            .apply("Saving To BigQuery", BigQueryIO.writeTableRows()
                .to(tableRef)
                .withCreateDisposition(CreateDisposition.CREATE_IF_NEEDED)
                .withWriteDisposition(WriteDisposition.WRITE_APPEND)
                .withSchema(tableSchema));

        p.run().waitUntilFinish();
    }

    interface SaveAsBigQueryTableOptions extends PipelineOptions {
        @Description("Path of the twitter data")
        String getTwitterData();
        void setTwitterData(String value);

        @Description("Output path")
        String getOutput();
        void setOutput(String value);

        @Description("BigQuery Project ID")
        String getProjectId();
        void setProjectId(String value);

        @Description("BigQuery Dataset ID")
        String getDataSetId();
        void setDataSetId(String value);

        @Description("BigQuery Table ID")
        String getTableId();
        void setTableId(String value);

    }
}
