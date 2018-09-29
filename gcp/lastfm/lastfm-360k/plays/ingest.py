#!/usr/bin/env python
"""Ingest & process data into BigQuery using Dataflow

"""
import apache_beam as beam
import argparse
from apache_beam.options.pipeline_options import (
    PipelineOptions,
    StandardOptions,
    GoogleCloudOptions,
    DebugOptions,
    WorkerOptions,
)
from apache_beam.io.gcp.internal.clients import bigquery


@beam.typehints.with_input_types(str)
def process(record):
    el = record.split('\t')
    if len(el) != 4:
        yield
    else:
        yield {
            'user_id': el[0],
            'mbid': el[1],
            'artist': el[2],
            'plays': int(el[3])
        }

def ingest_plays(p, gcs_path, bq_table):

    schema = bigquery.TableSchema(
        fields=[
            bigquery.TableFieldSchema(name='user_id',
                                      type='string',
                                      mode='required'),
            bigquery.TableFieldSchema(name='mbid',
                                      type='string',
                                      mode='required'),
            bigquery.TableFieldSchema(name='artist',
                                      type='string',
                                      mode='required'),
            bigquery.TableFieldSchema(name='plays',
                                      type='integer',
                                      mode='required')
        ])
    d = (p
         | 'ReadFromGCS' >> beam.io.ReadFromText(gcs_path)
         | 'ParseRecords' >> beam.FlatMap(process)
         | 'WriteToBigQuery' >> beam.io.WriteToBigQuery(
                bq_table,
                schema=schema,
                create_disposition=beam.io.BigQueryDisposition.CREATE_IF_NEEDED,
                write_disposition=beam.io.BigQueryDisposition.WRITE_TRUNCATE)
         )


if __name__ == '__main__':

    parser = argparse.ArgumentParser()
    parser.add_arguments('--plays-input',
                         help='Input files')
    parser.add_arguments('--plays-output',
                         help='BigQuery output table'
                         )
    known_args, pipeline_args = parser.parse_known_args()

    pipeline_options = PipelineOptions(pipeline_args)
    standard_options = pipeline_options.view_as(StandardOptions)
    gcp_options = pipeline_options.view_as(GoogleCloudOptions)
    debug_options = pipeline_options.view_as(DebugOptions)
    worker_options = pipeline_options.view_as(WorkerOptions)

    # Default to Dataflow
    if standard_options.runner is None:
        standard_options.runner = 'DataflowRunner'

    with beam.Pipeline(options=pipeline_options) as p:
        ingest_plays(p,
                     known_args.plays_input,
                     known_args.plays_output)