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

def process(record):
    """Parse a record
    """
    el = record.split('\t')
    if len(el) != 6:
        yield
    else:
        yield {
            'user_id': el[0],
            'ts': el[1],
            'artist_mbid': el[2],
            'artist_name': el[3],
            'track_mbid': el[4],
            'track_name': el[5]
        }

def ingest_listens(p, gcs_path, bq_table):

    schema = bigquery.TableSchema(
        fields=[
            bigquery.TableFieldSchema(name='user_id',
                                      type='string',
                                      mode='required'),
            bigquery.TableFieldSchema(name='ts',
                                      type='timestamp',
                                      mode='required'),
            bigquery.TableFieldSchema(name='artist_mbid',
                                      type='string',
                                      mode='required'),
            bigquery.TableFieldSchema(name='artist_name',
                                      type='string',
                                      mode='required'),
            bigquery.TableFieldSchema(name='track_mbid',
                                      type='string',
                                      mode='required'),
            bigquery.TableFieldSchema(name='track_name',
                                      type='string',
                                      mode='required'),
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
    parser.add_arguments('--listens-input',
                         help='Input files')
    parser.add_arguments('--listens-output',
                         help='BigQuery output table'
                         )
    known_args, pipeline_args = parser.parse_known_args()

    pipeline_options = PipelineOptions(pipeline_args)
    standard_options = pipeline_options.view_as(StandardOptions)
    gcp_options = pipeline_options.view_as(GoogleCloudOptions)
    debug_options = pipeline_options.view_as(DebugOptions)
    worker_options = pipeline_options.view_as(WorkerOptions)

    with beam.Pipeline(options=pipeline_options) as p:
        ingest_listens(p,
                       known_args.listens_input,
                       known_args.listens_output)