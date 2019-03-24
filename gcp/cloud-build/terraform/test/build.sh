#!/bin/sh

PROJECT_ID=$(gcloud config get-value core/project)

gcloud builds submit \
    --config=cloudbuild.yaml \
    --substitutions=\
_SOURCE_PROJECT=${PROJECT_ID}

