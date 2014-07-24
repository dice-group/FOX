[1]: http://repo1.maven.org/maven2/edu/stanford/nlp/stanford-corenlp/3.2.0/stanford-corenlp-3.2.0-models.jar
[2]: http://cogcomp.cs.illinois.edu/download/software/45
[3]: http://sourceforge.net/projects/balie
[4]: http://aksw.org/Projects/FOX.html
[5]: http://139.18.2.164:4444/demo/index.html#!/home

FOX - Federated Knowledge Extraction Framework
==============================================

Requirements
---
Java 7, Maven 3, graphviz


Installation
---
* Clone the latest version:
 `git clone -b master https://github.com/AKSW/FOX.git`

* Build the release:
  `./fox_build.sh`
  (This will download [stanford models][1] to `FOX/lib`.)

* Go into the release folder and rename `fox.properties-dist` to `fox.properties` and change the file to your needs.

* Learn with trainings data (optional with default properties file):
  `./fox_train.sh` (set training to true in  `fox.properties`)

* Start the server:
  `./fox_run.sh`

* Stop the server:
  `./fox_close.sh`

Demo and Documentation
----
[FOX 2.2.0][4]

Datasets
----
The training and testing datasets are in the `FOX/input` folder.

The resulting raw data from the learning and testing process are in the `FOX/evaluation` folder.

License
----
FOX is licensed under the [GNU General Public License Version 3, 29 June 2007](http://www.gnu.org/licenses/gpl-3.0.txt) (license document is in the application folder).