#!/bin/sh

mvn compile \
    exec:java \
    -Dexec.mainClass=ltd.cylleneworks.sandbox.beam.wordcount.MinimalWordCount \
    "-Dexec.args=data/input/* data/output/wordcount"