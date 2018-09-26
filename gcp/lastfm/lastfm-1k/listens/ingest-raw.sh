#!/bin/sh
##
## Load raw data into BigQuery
##

PROJECT_ID=$(gcloud config list --format 'value(core.project)')
GCS_SOURCE=gs://${PROJECT_ID}/lastfm-dataset-1K/userid-timestamp-artid-artname-traid-traname.tsv
BQ_TARGET=lastfm_1k.listens_raw
SCHEMA=user_id:string,ts:timestamp,artist_mbid:string,artist_name:string,track_mbid:string,track_name:string

bq load \
   -F "\t" \
   --quote "" \
   ${BQ_TARGET} ${GCS_SOURCE} \
   ${SCHEMA}
