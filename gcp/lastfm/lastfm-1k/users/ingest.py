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

import json

def process(record):
    """Parse record and convert to correct BigQuery data type
    """
    from datetime import datetime

    def parse_int(value):
        try:
            return int(value)
        except ValueError as ex:
            return None

    def parse_date(value):
        try:
            d = datetime.strptime(value, '%b %d, %Y')
            t = d - datetime(1970, 1, 1)
            return (t.microseconds + (t.seconds + t.days * 86400) * 10**6)/10**6
        except ValueError as ex:
            return None

    el = [ a.strip() for a in record.split('\t') ]
    if len(el) != 5:
        yield
    else:
        yield {
            'user_id': el[0],
            'gender': el[1],
            'age': parse_int(el[2]),
            'country': el[3],
            'signup': parse_date(el[4])
        }

def ingest_users(p, gcs_path, bq_table):

    schema = bigquery.TableSchema(
        fields=[
            bigquery.TableFieldSchema(name='user_id',
                                      type='string',
                                      mode='required'),
            bigquery.TableFieldSchema(name='gender',
                                      type='string',
                                      mode='nullable'),
            bigquery.TableFieldSchema(name='age',
                                      type='integer',
                                      mode='nullable'),
            bigquery.TableFieldSchema(name='country',
                                      type='string',
                                      mode='nullable'),
            bigquery.TableFieldSchema(name='signup',
                                      type='timestamp',
                                      mode='nullable')
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
    parser.add_arguments('--users-input',
                         help='Input files')
    parser.add_arguments('--users-output',
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
        ingest_users(p,
                     known_args.users_input,
                     known_args.users_output)
