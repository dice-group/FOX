#!/bin/bash

file="lib/stanford-corenlp-3.2.0-models.jar"
url="http://repo1.maven.org/maven2/edu/stanford/nlp/stanford-corenlp/3.2.0/stanford-corenlp-3.2.0-models.jar"

if [ ! -f "$file" ]; then
    echo "Downloading dependencies ... ($url)"
    curl --retry 4 -o "$file" "$url"
fi
if [ -f "$file" ]; then
    mvn clean install javadoc:javadoc
else
    echo "Couldn't download dependency: $url"
fi