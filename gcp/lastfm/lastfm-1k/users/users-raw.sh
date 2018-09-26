#!/bin/sh
##
## Load raw data into BigQuery
##

PROJECT_ID=$(gcloud config list --format 'value(core.project)')
GCS_SOURCE=gs://${PROJECT_ID}/lastfm-dataset-1K/userid-profile.tsv
BQ_TARGET=lastfm_1k.users_raw
SCHEMA=user_id:string,gender:string,age:integer,country:string,signup:string

bq load \
   -F "\t" \
   --skip_leading_rows 1 \
   ${BQ_TARGET} ${GCS_SOURCE} \
   ${SCHEMA}