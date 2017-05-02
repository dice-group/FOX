#!/bin/sh
export MAVEN_OPTS="-Xmx16G -Dlog4j.configuration=file:data/fox/log4j.properties"
#
#
input=eval/MultilayerPerceptron_total.csv
output=eval/Measures_MultilayerPerceptron_total.csv
#
#

nohup mvn exec:java -Dexec.mainClass="org.aksw.fox.utils.evaluation.FoxEvaluationHelper" -Dexec.args="-i$input -o$output -mfalse" > measures.log &



# java -cp fox-${project.version}-jar-with-dependencies.jar org.aksw.fox.utils.evaluation.FoxEvaluationHelper -i$input -o$output -mfalse
# echo "Done. Check the $output file."
