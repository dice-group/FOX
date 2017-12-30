[1]: ../src/main/java/org/aksw/fox/tools/ner/INER.java
[2]: ../src/main/java/org/aksw/fox/data/EntityClassMap.java


## Extend FOX with other Named Entity Recognition (NER) tools

To integrate other NER tools into FOX you have to implement the interface: [NER.java][1]. Make sure that the tools support at least the entity types: 'LOCATION', 'ORGANIZATION' and 'PERSON' defined in [EntityClassMap.java][2].

## Learn a new model for NER

Once you have integrate NER tools into FOX, FOX needs to be  retrained with the
ensemble learning.

At first you have to set up the 'fox.properties' file.

The value of org.aksw.fox.tools.Tools.lifeTime describes how long each NER tool will life in minutes. For the learning mode you have to set a high value, otherwise it could be that the tools will stop in the middle of the learning process.

To not override a learned model by mistake there is a lock. Set org.aksw.fox.nerlearner.FoxClassifier.training to true.

With org.aksw.fox.nerlearner.FoxClassifier.learner you are able to specify which classifier should be used and with org.aksw.fox.nerlearner.FoxClassifier.modelPath where the learned models will be stored.

The values of org.aksw.fox.nerlearner.reader.NERReaderFactory.readerclass are the class for the training data.

To read the training data, choose the right reader in org.aksw.fox.nerlearner.reader.NERReaderFactory.readerclass. For the training data input/{1,2,3,4,5} choose org.aksw.fox.nerlearner.reader.TrainingInputReader and for the training data in input/Wikiner/* choose org.aksw.fox.nerlearner.reader.WikinerReader.

In case you take input/Wikiner as training data, you could specify how many sentences will be use with org.aksw.fox.nerlearner.reader.INERReader.maxSentences because the datasets are big.

### Now you are ready to learn a new model.
Edit the ./ScriptLearn.sh file to set the input data you like to use.
The arguments are:

-l for the language, e.g.: en, de, ...

-a is the action here you have to choose 'train'

-i for the input data, e.g. 'input/Wikiner/aij-wikiner-en-wp3.bz2'

Run the script.

After that, you should have a new model. To use it, simple change org.aksw.fox.nerlearner.FoxClassifier.training back to false and org.aksw.fox.tools.ner.FoxNERTools.foxNERLifeTime back to 2 min.

## Evaluate a new model

In this scenario the model will not be stored. To get a new model use the previous section. It is important to read the section 'Learn a new model for NER'. Since this steps are required here also.

In 'fox.properties' file:

With `org.aksw.fox.nerlearner.FoxClassifier.learner` you are able to specify which classifier should be used. It seems that the `weka.classifiers.functions.MultilayerPerceptron`, `weka.classifiers.meta.AdaBoostM1` with the option `org.aksw.fox.nerlearner.FoxClassifier.learnerOptions:-W weka.classifiers.trees.J48` and `weka.classifiers.trees.RandomForest` are a good choice (for more and detailed information read the paper).

To evaluate one NER tool alone, put that tool there and choose as classifier `result_vote`.

In case you choose `input/Wikiner` as dataset, you could specify how many sentences will be use with `org.aksw.fox.nerlearner.reader.INERReader.maxSentences` because the datasets are big.
(30000 sentences need around 2h within a 10-fold cross validation on a i5-core)

With the value of `org.aksw.fox.CrossValidation.runs` you are able to set up how many repeats of the 10-fold cross-validation will be performed.

### Now you are ready to evaluate a new model.

Edit the `ScriptValidate.sh` file to set the input data you like to use and run the script.

After that, you should have a folder `eval` that contain at least two files for your current chosen classifier with the confusion matrix.
The bigger file with the classification for each repeat of the 10-fold cross validation and for each fold.
The smaller file contains the values in total for each repeat. In the most cases the smaller file is of interest.

With the confusion matrix you now able to calculate recall, precision, f-score and so on (https://en.wikipedia.org/wiki/Confusion_matrix).

An example of the matrix looks like this:

|   classifier	|   class	|   a	|   b	|  c 	|  d 	|
|---	|---	|---	|---	|---	|---	|
|   MultilayerPerceptron	|LOCATION|   	   3096	|  128 	|152| 1455|
|   MultilayerPerceptron	|  ORGANIZATION 	|  225 	|  8488 	|  156 	| 2096  	|
|   MultilayerPerceptron	|   PERSON	|  128 	|   59	|  7529 	|   442	|
|   MultilayerPerceptron	|NULL|1695|  1871 	|402|  205285 	|

That means that 3096 +    128    +152+   1455=4831 entities with type LOCATION are in the dataset, 3096 are correctly classified and 128 falsely classified as ORGANIZATION.

<!--
To calculate the measures (recall, precision, f-score) there is a script `measures.sh`. Change the script to your needs (i.e.: input and output file). Run the script and it will write a file with the measures. To run the script, the 'fox.properties' files has to be the same as in the experiment.-->
