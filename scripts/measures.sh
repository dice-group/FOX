That script needs an update
#!/bin/sh
#
# That script helps you to calculate the measures. 
# Please change the variables 'input' and 'output' to your needs.
# And run the script in the release folder.
#
input=eval/MultilayerPerceptron_total.csv
output=eval/Measures_MultilayerPerceptron_total.csv
#
#
java -cp fox-2.3.0-jar-with-dependencies.jar org.aksw.fox.utils.evaluation.FoxEvaluationHelper -i$input -o$output -mfalse
echo "Done. Check the $output file."