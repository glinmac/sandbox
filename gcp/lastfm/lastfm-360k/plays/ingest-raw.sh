#!/bin/sh
## Load raw data

PROJECT_ID=$(gcloud config list --format 'value(core.project)')
GCS_SOURCE=gs://${PROJECT_ID}/lastfm-dataset-360K/usersha1-artmbid-artname-plays.tsv
BQ_TARGET=lastfm_360k.plays_raw
SCHEMA=user_id:string,mbid:string,artist:string,plays:integer

bq load \
   -F "\t" \
   --quote "" \
   ${BQ_TARGET} ${GCS_SOURCE} \
   ${SCHEMA}
