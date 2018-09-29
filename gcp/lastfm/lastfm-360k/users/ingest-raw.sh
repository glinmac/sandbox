#!/bin/sh
## Load raw data into BigQuery

PROJECT_ID=$(gcloud config list --format 'value(core.project)')
GCS_SOURCE=gs://${PROJECT_ID}/lastfm-dataset-360K/usersha1-profile.tsv
BQ_TARGET=lastfm_360k.users_raw
SCHEMA=user_id:string,gender:string,age:integer,country:string,signup:string

bq load \
   -F "\t" \
   ${BQ_TARGET} ${GCS_SOURCE} \
   ${SCHEMA}
