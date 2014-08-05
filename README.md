[1]: http://repo1.maven.org/maven2/edu/stanford/nlp/stanford-corenlp/3.2.0/stanford-corenlp-3.2.0-models.jar
[2]: http://cogcomp.cs.illinois.edu/download/software/45
[3]: http://sourceforge.net/projects/balie
[4]: http://fox.aksw.org
[5]: http://139.18.2.164:4444/demo/index.html#!/home
[6]: http://fox-demo.aksw.org

##FOX - Federated Knowledge Extraction Framework
FOX is a framework that integrates the Linked Data Cloud and makes use of the diversity of NLP algorithms to extract RDF triples of high accuracy out of NL. 
In its current version, it integrates and merges the results of Named Entity Recognition tools. 
Keyword Extraction and Relation Extraction tools will be merged soon.

##Requirements
Java 7, Maven 3, graphviz


##Installation
:one: Clone the latest version: `git clone -b master https://github.com/AKSW/FOX.git`

:two: Download [NETagger][2]. This archive contains a `data` and `config` folder. Copy both to the root folder `FOX`.

:three: Build the release: `./fox_build.sh` (This will download [stanford models][1] to `FOX/lib`.)

:four: Go into the `release` folder and rename `fox.properties-dist` to `fox.properties` and change the file to your needs.

:five: Learn with trainings data (optional with default properties file): `./fox_train.sh` (set training to true in  `fox.properties`)

:six: Start the server: `./fox_run.sh`

:seven: Stop the server: `./fox_close.sh`

##Demo and Documentation
Project Page: [http://fox.aksw.org](http://fox.aksw.org)

Live Demo: [http://fox-demo.aksw.org (Version 2.2.2) ](http://fox-demo.aksw.org)

##Datasets
The training and testing datasets are in the `FOX/input` folder.

The resulting raw data from the learning and testing process are in the `FOX/evaluation` folder.

##License
FOX is licensed under the [GNU General Public License Version 2, June 1991](http://www.gnu.org/licenses/gpl-2.0.txt) (license document is in the application folder).

##Bugs
Found a :bug: bug? [Open an issue](https://github.com/AKSW/fox/issues/new) with some [emojis](http://emoji.muan.co). Issues without emojis are not valid. :trollface:

##Changelog
### [v2.2.2]
* AGDISTIS endpoint in `fox.properties` file
* server framework version update
* error pages
* fix server pool issue
* other minor changes

### [v2.2.1](https://github.com/AKSW/FOX/releases/tag/v2.2.1)
* installation update, because of an update of Illinois NER
* other minor changes

### [v2.2.0](https://github.com/AKSW/FOX/releases/tag/v2.2.0)
