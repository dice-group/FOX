[1]:  http://repo1.maven.org/maven2/edu/stanford/nlp/stanford-corenlp/3.2.0/stanford-corenlp-3.2.0-models.jar
[2]: http://cogcomp.cs.illinois.edu/download/software/28
[3]: http://sourceforge.net/projects/balie
[fox]: http://aksw.org/Projects/FOX.html
[doc]: http://139.18.2.164:4444/demo/index.html#!/home

FOX - Federated Knowledge Extraction Framework
==============================================

Installation
------------
* `git clone -b master git@github.com:AKSW/FOX.git`
clones the latest version

* `./fox_build.sh`
downloads dependencies and builds the target

* in the target folder, rename `fox.properties-dist` to `fox.properties` and chance the file to your needs

* `./target/fox_run_server.sh`
starts the server

Demo
----
[FOX 2.0][fox]

Documentation
----
[FOX 2.0 doc][doc]
