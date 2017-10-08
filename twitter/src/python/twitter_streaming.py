#!/usr/bin/env python
"""
small tools to fetch data from the twitter stream
"""
import twitter
import json
import argparse
import sys
from datetime import datetime, timedelta

consumer_key = ''
consumer_secret = ''
access_token_key = ''
access_token_secret = ''

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--output', help='Output file to store tweets')
    parser.add_argument('--tweets-count', dest='tweets_count', type=int,
                        help='Number of tweets to process from the stream')
    args = parser.parse_args()

    api = twitter.Api(consumer_key=consumer_key,
                      consumer_secret=consumer_secret,
                      access_token_key=access_token_key,
                      access_token_secret=access_token_secret)

    tweet_count = 0

    start = datetime.utcnow().replace(minute=0, second=0, microsecond=0)
    next_hour = start + timedelta(hours=1)

    output = open(args.output % start.strftime('%Y%m%d%H%M%S'), 'w') if args.output else sys.stdout

    for tweet in api.GetStreamFilter(track='music,musique,audio,song,release,chart'):

        output.write(json.dumps(tweet))
        output.write('\n')

        tweet_count += 1

        # break early
        if args.tweets_count and tweet_count >= args.tweets_count:
            break

        # small progress
        if not tweet_count % 1000:
            print(tweet_count)

        # rollover to next file
        if datetime.utcnow() >= next_hour:
            output.close()
            output = open(args.output % next_hour.strftime('%Y%m%d%H%M%S'), 'w') if args.output else sys.stdout
            next_hour += timedelta(hours=1)
