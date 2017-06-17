#!/bin/sh
export MAVEN_OPTS="-Xmx16G -Dlog4j.configuration=file:data/fox/log4j.properties"

#ARGS="-len -atrain -iinput/4"
ARGS="-len -atrain -iinput/Wikiner/aij-wikiner-en-wp3.bz2"


nohup mvn exec:java -Dexec.mainClass="org.aksw.fox.ui.FoxCLI" -Dexec.args="$ARGS" > logLearn.log &

#nohup java -Xmx8G -cp fox-${project.version}-jar-with-dependencies.jar org.aksw.fox.FoxCLI -lde -atrain -iinput/Wikiner/aij-wikiner-de-wp3.bz2 > learn.log &