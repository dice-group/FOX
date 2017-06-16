#!/bin/sh
export MAVEN_OPTS="-Xmx16G -Dlog4j.configuration=file:data/fox/log4j.properties"

cd spotlight

./runDE.sh
./runEN.sh
./runEs.sh
./runNL.sh
./runFR.sh

cd ..


nohup mvn exec:java  -Dexec.mainClass="org.aksw.fox.ui.FoxRESTful" > logRun.log &
