#!/bin/sh
export MAVEN_OPTS="-Xmx16G -Dlog4j.configuration=file:data/fox/log4j.properties"

#ARGS="-len -atrain -iinput/4"
ARGS="-len -atrain -iinput/Wikiner/aij-wikiner-en-wp3.bz2"
#ARGS="-len -atrain -iinput/bengal/bengal_hybrid_10000.ttl"

nohup mvn exec:java -Dexec.mainClass="org.aksw.fox.ui.FoxCLI" -Dexec.args="$ARGS" >  $0.log &