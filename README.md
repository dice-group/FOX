[1]: http://repo1.maven.org/maven2/edu/stanford/nlp/stanford-corenlp/3.2.0/stanford-corenlp-3.2.0-models.jar
[2]: http://cogcomp.cs.illinois.edu/download/software/45
[3]: http://sourceforge.net/projects/balie
[4]: http://aksw.org/Projects/FOX.html
[5]: http://139.18.2.164:4444/demo/index.html#!/home

FOX - Federated Knowledge Extraction Framework
==============================================

Requirements
---
Java 7 and Maven 3.

Installation
---
* Clone the latest version:
 `git clone -b master git@github.com:AKSW/FOX.git`

* Download [NETagger][2]. This archive contains a `data`, `config`, `lib` and  `dist` folder. Copy the first two to the FOX root.
  The `lib` folder  contains `LBJLibrary-2.8.2.jar` and `LbjNerTagger-2.3.jar` and the `dist` folder contains `LbjNerTagger-2.3.jar`.
  Copy this three files to `FOX/lib/illinois`.

* Download [stanford models][1] to `FOX/lib`.

* Build the target:
  `./fox_build.sh`
  
* Go into the target folder and rename `fox.properties-dist` to `fox.properties` and change the file to your needs.

* Learn with trainings data (optional with default properties file):
  `./fox_training.sh` (set training to true in  `fox.properties`)

* Start the server:
  `./fox_run_server.sh`

* Stop the server:
  `./fox_close.sh`


Demo
----
[FOX 2.0][4]

Documentation
----
[FOX 2.0 doc][5]
