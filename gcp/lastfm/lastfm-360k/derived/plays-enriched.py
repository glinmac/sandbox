#!/usr/bin/env python
"""Join dataset reading from BigQuery
Output the result using an Elasticsearch compatible format (bulk)
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

def process(record, users):
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

def join_users_plays(p, bq_dataset='lastfm', plays_table='plays', users_table='users', output_path=None):
    """Join Plays to users to enrich plays with users data
    Use a side input as the users table is small (~350k records)
    """
    assert output_path is not None
    users_query = """
SELECT
    user_id,
    gender,
    age,
    country,
    UNIX_MILLIS(signup) as signup
FROM {}.{}""".format(bq_dataset, users_table)
    plays = (p
             | 'ReadPlays' >> beam.io.Read(
                beam.io.BigQuerySource(query='SELECT * FROM {}.{}'.format(bq_dataset, plays_table))))

    users = (p
             | 'ReadUsers' >> beam.io.Read(beam.io.BigQuerySource(query=users_query, use_standard_sql=True))
             | 'FormatUsers' >> beam.Map(format_users))

    joined_data = (plays
                   | 'JoinData' >> beam.Map(process, users=beam.pvalue.AsDict(users))
                   | 'FormatESBulk' >> beam.Map(format_es_bulk)
                   | 'Write' >> beam.io.WriteToText(output_path))

if __name__ == '__main__':

    parser = argparse.ArgumentParser()
    parser.add_arguments('--bq-dataset-output',
                         default='lastfm_360k',
                         help='BigQuery dataset to use in output')
    parser.add_arguments('--bq-table-plays',
                         help='BigQuery tablename for plays')
    parser.add_arguments('--bq-table-users',
                         help='BigQuery tablename for users')
    parser.add_arguments('--plays-enriched-output',
                         help='Output file for enriched plays')


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
        join_users_plays(p,
                         bq_dataset=known_args.bq_dataset_output,
                         plays_table=known_args.bq_table_plays,
                         users_table=known_args.bq_table_users,
                         output_path=known_args.plays_enriched_output)