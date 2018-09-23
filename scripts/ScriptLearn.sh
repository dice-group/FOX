#!/bin/sh
#
export MAVEN_OPTS="-Xmx16G -Dlog4j.configuration=file:data/fox/log4j.properties"
ARGS="-len -atrain -iinput/Wikiner/aij-wikiner-en-wp3.bz2"
#
echo "##############################################################################" 
echo "# Check $0.log file."
echo "##############################################################################" 
#
nohup mvn exec:java -Dexec.mainClass="org.aksw.fox.ui.FoxCLI" -Dexec.args="$ARGS" > $0.log 2>&1 </dev/null &