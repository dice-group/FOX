#!/bin/bash
echo "Checking dependencies..."
pkg="graphviz"
if dpkg --get-selections | grep -q "^$pkg[[:space:]]*install$" >/dev/null; then
     echo "$pkg is already installed"
else
    echo "Please install: $pkg"
    echo "(sudo apt-get install $pkg)"
    echo
fi
#
# build fox
echo "Building FOX..."
if [ -f "$file" ] & dpkg --get-selections | grep -q "^$pkg[[:space:]]*install$" >/dev/null; then
    nohup mvn clean compile -Dmaven.test.skip=true javadoc:javadoc > build.log &
else
    echo "Couldn't build FOX."
fi
#
