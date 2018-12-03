#!/bin/sh
#
export MAVEN_OPTS="-Xmx16G -Dlog4j.configuration=file:data/fox/log4j.properties"
#
echo "##############################################################################" 
echo "# Check $0.log file."
echo "##############################################################################" 
#
nohup mvn exec:java  -Dexec.mainClass="org.aksw.fox.ui.FoxRESTful" > $0.log 2>&1 </dev/null &
