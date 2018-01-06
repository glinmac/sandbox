#!/usr/bin/env bash -x


bucket=$1
region=us-central1
topic=hello_world
memory=128MB

deploy() {
    gcloud beta functions deploy helloWorld \
        --stage-bucket ${bucket} \
        --region ${region} \
        --memory ${memory} \
        --trigger-topic ${topic}
}

logs() {
    gcloud beta functions logs read helloWorld
}


test() {
    gcloud beta functions call helloWorld \
        --data '{"message":"Hello World!"}'
}

describe() {
    gcloud beta functions describe helloWorld
}

delete () {
    gcloud beta functions delete helloWorld
}

$1

