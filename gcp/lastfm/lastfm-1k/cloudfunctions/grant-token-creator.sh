#!/bin/sh
## Allow appspot to request token

PROJECT_ID=$1

gcloud iam service-accounts add-iam-policy-binding \
    ${PROJECT_ID}@appspot.gserviceaccount.com \
     --member=serviceAccount:${PROJECT_ID}@appspot.gserviceaccount.com \
    --role=roles/iam.serviceAccountTokenCreator