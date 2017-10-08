#!/usr/bin/env python
"""
Spark + Twitter data
"""
__author__ = 'Guillaume Gardey <glinmac@gmail.com>'

import argparse

from pyspark import SparkContext, SparkConf
from pyspark.sql import SQLContext, Row

import json

def get_stream_record_type(record):
    if 'delete' in record:
        return 'delete'
    if 'scrub_geo' in record:
        return 'scrub_geo'
    if 'limit' in record:
        return 'limit'
    if 'status_withheld' in record:
        return 'status_withheld'
    if 'user_withheld' in record:
        return 'user_withheld'
    if 'disconnect' in record:
        return 'disconnect'
    if 'warning' in record:
        return 'warning'
    if 'user_update' in record:
        return 'user_update'
    if 'id' in record:
        return 'tweet'
    else:
        return 'unknown'

if __name__ == '__main__':

    parser = argparse.ArgumentParser()
    parser.add_argument('input_file', metavar='INPUT', help='Input file to process')

    args = parser.parse_args()

    input_file = args.input_file

    conf = SparkConf().setAppName("Spark Twitter").setMaster("local")
    sc = SparkContext(conf=conf)
    sqlContext = SQLContext(sc)

    raw_stream = sc.textFile(input_file).cache()

    # Analyse records in the stream
    record_categories = raw_stream.map(lambda line: json.loads(line)) \
        .map(lambda o: (get_stream_record_type(o), 1)) \
        .reduceByKey(lambda a,b: a+b)

    print(record_categories.collect())

    # filter tweets only and do some projections
    tweets = raw_stream.map(lambda line: json.loads(line)) \
        .filter(lambda o: get_stream_record_type(o) == 'tweet') \
        .map(lambda a: Row(id=a['id'], text=a['text'], created_at=a['created_at']))

    # create a DataFrame for query
    df = sqlContext.createDataFrame(tweets)
    df.show()


