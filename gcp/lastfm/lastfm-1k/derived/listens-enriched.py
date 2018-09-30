#!/usr/bin/env python
"""Join example
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

def parse_users(record):
    """
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

def parse_listens(record):
    """Parse raw listens data
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

def format_users(user):
    """Format compatible with Side Input / AsDict
    """
    return (user['user_id'],
            {
                'country': user['country'],
                'gender': user['gender'],
                'age': user['age'],
                'signup': user['signup']
            })

def format_listens_enriched_bq(record):
    """Format the enriched listens into a format compatible
    with the BigQuery schema
    """
    return {
        'ts': record['ts'],
        'artist': {
            'mbid': record['artist_mbid'],
            'name': record['artist_name'],
        },
        'track': {
            'mbid': record['track_mbid'],
            'name': record['track_name'],
        },
        'user': {
            'user_id': record['user_id'],
            'gender': record['gender'],
            'signup': record['signup'],
            'country': record['country'],
            'age': record['age'],
        }
    }

def do_join(record, users):
    """Enrich a play with the detail about the users
    """
    if record['user_id'] in users:
        record.update(users[record['user_id']])
    return record

def format_es_bulk(record):
    """Small util to write output in an ES bulk load compatible way
    """
    import json
    # We don't specify _index, _type or _id
    # Correct end point need to be used when doing the bulk load
    # /<INDEX>/<MAPPING>/_bulk
    return '{"index":{}}\n' + json.dumps(record)

def process(p,
            project=None,
            users_path=None,
            listens_path=None,
            bq_dataset=None,
            listens_table=None,
            listens_enriched_table=None,
            users_table=None,
            listens_enriched_path=None):
    """Join listens to users to enrich listens with users data
    Use a side input as the users table is small (~350k records)
    """

    users_schema = bigquery.TableSchema(
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

    listens_schema = bigquery.TableSchema(
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

    # We changed a bit the schema to show how to use nested records
    listens_enriched_schema = bigquery.TableSchema(
        fields=[
            bigquery.TableFieldSchema(name='ts',
                                      type='timestamp',
                                      mode='required'),
            bigquery.TableFieldSchema(name='artist',
                                      type='record',
                                      mode='nullable',
                                      fields=[
                                          bigquery.TableFieldSchema(name='mbid',
                                                                    type='string',
                                                                    mode='required'),
                                          bigquery.TableFieldSchema(name='name',
                                                                    type='string',
                                                                    mode='required'),
                                      ]),
            bigquery.TableFieldSchema(name='track',
                                      type='record',
                                      mode='required',
                                      fields=[
                                          bigquery.TableFieldSchema(name='mbid',
                                                                    type='string',
                                                                    mode='required'),
                                          bigquery.TableFieldSchema(name='name',
                                                                    type='string',
                                                                    mode='required'),
                                      ]),
            bigquery.TableFieldSchema(name='user',
                                      type='record',
                                      mode='required',
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
                                                                    mode='nullable'),
                                      ])
        ]
    )

    # Read and parse users from GCS
    users = (p
             | 'ReadUsersFromGCS' >> beam.io.ReadFromText(users_path)
             | 'ParseUsersRecords' >> beam.FlatMap(parse_users))

    # Write users to BigQuery
    users_bq = (users
                | 'WriteUsersToBigQuery' >> beam.io.WriteToBigQuery(
                '{}:{}.{}'.format(project, bq_dataset, users_table),
                schema=users_schema,
                create_disposition=beam.io.BigQueryDisposition.CREATE_IF_NEEDED,
                write_disposition=beam.io.BigQueryDisposition.WRITE_TRUNCATE))

    # Format users for Join
    users_for_join = (users
                      | 'FormatUsersForJoin' >> beam.Map(format_users))

    # Read and parse listens from GCS
    listens = (p
               | 'ReadListensFromGCS' >> beam.io.ReadFromText(listens_path)
               | 'ParseListensRecords' >> beam.FlatMap(parse_listens))

    # Write listens to BigQuery
    listens_bq = ( listens
                   | 'WriteListensToBigQuery' >> beam.io.WriteToBigQuery(
                '{}:{}.{}'.format(project, bq_dataset, listens_table),
                schema=listens_schema,
                create_disposition=beam.io.BigQueryDisposition.CREATE_IF_NEEDED,
                write_disposition=beam.io.BigQueryDisposition.WRITE_APPEND))

    # Join listens with users data
    listens_enriched = (listens
                        | 'JoinListensWithUsers' >> beam.Map(do_join,
                                                             users=beam.pvalue.AsDict(users_for_join)))

    # Write the join to BigQuery
    listens_enriched_bq = (listens_enriched
                           | 'FormatListensEnrichedBgiQuery' >> beam.Map(format_listens_enriched_bq)
                           | 'WriteListendEnrichedToBigQuery' >> beam.io.WriteToBigQuery(
                '{}:{}.{}'.format(project, bq_dataset, listens_enriched_table),
                schema=listens_enriched_schema,
                create_disposition=beam.io.BigQueryDisposition.CREATE_IF_NEEDED,
                write_disposition=beam.io.BigQueryDisposition.WRITE_APPEND))

    # Write the join to ES bulk load text format
    listens_enriched_es = (listens_enriched
                           | 'FormatESBulk' >> beam.Map(format_es_bulk)
                           | 'Write' >> beam.io.WriteToText(listens_enriched_path))

if __name__ == '__main__':

    parser = argparse.ArgumentParser()
    parser.add_arguments('--listens-input',
                         help='Input file for listens')
    parser.add_arguments('--users-input',
                         help='Input file for users')
    parser.add_arguments('--listens-enriched-output',
                         help='Output file for enriched data')
    parser.add_arguments('--bq-dataset-output',
                         default='lastfm_1k',
                         help='BigQuery dataset to use in output')
    parser.add_arguments('--bq-table-listens',
                         help='BigQuery tablename for listens')
    parser.add_arguments('--bq-table-listens-enriched',
                         help='BigQuery tablename for listens enriched')
    parser.add_arguments('--bq-table-users',
                         help='BigQuery tablename for users')

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
        process(p,
                bq_dataset=known_args.bq_dataset_output,
                listens_table=known_args.bq_table_listens,
                listens_enriched_table=known_args.bq_table_listens_enriched,
                users_table=known_args.bq_table_users,
                users_path=known_args.users_input,
                listens_path=known_args.listens_input,
                listens_enriched_path=known_args.listens_enriched_output
                )