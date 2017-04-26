#!/bin/sh
export MAVEN_OPTS="-Xmx16G -Dlog4j.configuration=file:data/fox/log4j.properties"

nohup mvn exec:java -Dexec.mainClass="org.aksw.fox.ui.FoxCLI" -Dexec.args="-len -atrain -iinput/1" > learn.log &


#nohup java -Xmx8G -cp fox-${project.version}-jar-with-dependencies.jar org.aksw.fox.FoxCLI -lde -atrain -iinput/Wikiner/aij-wikiner-de-wp3.bz2 > learn.log &