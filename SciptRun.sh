#!/bin/sh
export MAVEN_OPTS="-Xmx16G -Dlog4j.configuration=file:data/fox/log4j.properties"

# mvn clean compile -Dmaven.test.skip=falsejavadoc:javadoc && mvn exec:java  -Dexec.mainClass="org.aksw.fox.ui.FoxRESTful" -Dlog4j.configuration="file:log4j.properties"

echo "debug run" && mvn compile -Dmaven.test.skip=true && mvn exec:java  -Dexec.mainClass="org.aksw.fox.ui.FoxRESTful" 
