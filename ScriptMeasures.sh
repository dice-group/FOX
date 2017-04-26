#!/bin/sh
export MAVEN_OPTS="-Xmx16G -Dlog4j.configuration=file:data/fox/log4j.properties"
#
# That script helps you to calculate the measures. 
# Please change the variables 'input' and 'output' to your needs.
# And run the script in the release folder.
#
input=eval/MultilayerPerceptron_total.csv
output=eval/Measures_MultilayerPerceptron_total.csv
#
#
java -cp fox-${project.version}-jar-with-dependencies.jar org.aksw.fox.utils.evaluation.FoxEvaluationHelper -i$input -o$output -mfalse
echo "Done. Check the $output file."