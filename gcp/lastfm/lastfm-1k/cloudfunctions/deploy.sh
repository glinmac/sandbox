#!/bin/sh
## Example to deploy a Cloud Function

PROJECT=$1
REGION="europe-west1"
BUCKET="gs://${PROJECT}"
MEMORY="128M"
RUNTIME="nodejs8"
STAGE_BUCKET="gs://${PROJECT}-staging"
FUNCTION_NAME="gcsStagingTriggerDag"

gcloud functions deploy ${FUNCTION_NAME} \
       --region=${REGION} \
       --memory=${MEMORY} \
       --trigger-bucket ${BUCKET} \
       --runtime ${RUNTIME} \
       --source gcs-staging-trigger \
       --stage-bucket ${STAGE_BUCKET}