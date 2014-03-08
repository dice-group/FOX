#!/bin/bash

echo "Building FOX..."
# check installed dependencies
file="lib/stanford-corenlp-3.2.0-models.jar"
url="http://repo1.maven.org/maven2/edu/stanford/nlp/stanford-corenlp/3.2.0/stanford-corenlp-3.2.0-models.jar"
if [ ! -f "$file" ]; then
    echo "Downloading dependencies ... ($url)"
    curl --retry 4 -o "$file" "$url"
    if [ ! -f "$file" ]; then
        echo "Couldn't downloading dependency: $file"
    fi
fi

# check installed dependencies
pkg="graphviz"
if dpkg --get-selections | grep -q "^$pkg[[:space:]]*install$" >/dev/null; then
     echo "$pkg is already installed"
else
    echo "Please install: $pkg"
    echo "(sudo apt-get install $pkg)"
    echo
    #if apt-get install $pkg; then
    #    echo "Successfully installed $pkg"
    #else
    #    echo "Error installing $pkg"
    #fi
fi

# build fox
if [ -f "$file" ] & dpkg --get-selections | grep -q "^$pkg[[:space:]]*install$" >/dev/null; then
    mvn clean install javadoc:javadoc
else
    echo "Couldn't build FOX."
fi