#!/bin/sh
export MAVEN_OPTS="-Xmx16G -Dlog4j.configuration=file:data/fox/log4j.properties"

nohup mvn clean compile -Dmaven.test.skip=false test javadoc:javadoc > logCompile.log && nohup mvn exec:java  -Dexec.mainClass="org.aksw.fox.ui.FoxRESTful" > logRun.log &
