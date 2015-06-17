[4]: http://fox.aksw.org
[5]: http://fox-demo.aksw.org

[![Build Status](https://travis-ci.org/AKSW/FOX.svg?branch=master)](https://travis-ci.org/AKSW/FOX)
##FOX - Federated Knowledge Extraction Framework
FOX is a framework that integrates the Linked Data Cloud and makes use of the diversity of NLP algorithms to extract RDF triples of high accuracy out of NL. 
In its current version, it integrates and merges the results of Named Entity Recognition tools. 
Keyword Extraction and Relation Extraction tools will be merged soon.

##Requirements
Java 8, Maven 3, graphviz (for JavaDoc only)


##Installation
:one: Clone the latest version: `git clone -b master https://github.com/AKSW/FOX.git`

:two: Build the release: `./build.sh`

:three: Go into the `release` folder and rename `fox.properties-dist` to `fox.properties` and change the file to your needs.

:four: Learn with trainings data (optional with default properties file): `./learn.sh` (set `org.aksw.fox.nerlearner.FoxClassifier.training` to true in  `fox.properties`)

:five: Start the server: `./run.sh`

:six: Stop the server: `./close.sh`

##Demo and Documentation
Project Page: [http://fox.aksw.org][4]

Live Demo: [http://fox-demo.aksw.org (Version 2.2.2) ][5]

##Datasets
The training and testing datasets are in the `input` folder.

The resulting raw data from the learning and testing process are in the `evaluation` folder.

## How to cite
```Tex
@incollection{
  year={2014},
  isbn={978-3-319-11963-2},
  booktitle={The Semantic Web – ISWC 2014},
  volume={8796},
  series={Lecture Notes in Computer Science},
  title={Ensemble Learning for Named Entity Recognition},
  publisher={Springer International Publishing},
  author={Speck, René and Ngonga Ngomo, Axel-Cyrille}
}
```

##License
FOX is licensed under the [GNU GPL Version 2, June 1991](http://www.gnu.org/licenses/gpl-2.0.txt) (license document is in the application folder).

FOX uses several other libraries. An incomplete list is as follows:
* Illinois NLP Pipeline  (University of Illinois Research and Academic Use License)
* Stanford CoreNLP (GNU GPL Version 2)
* Apache OpenNLP (Apache License, Version 2)
* Balie (GNU GPL Version 2)

##Bugs
Found a :bug: bug? [Open an issue](https://github.com/AKSW/fox/issues/new) with some [emojis](http://emoji.muan.co). Issues without emojis are not valid. :trollface:

##Changelog
### [v2.2.2]
* Adds Stanfords models to pom.xml
* Illinois update and installation fixed
* RESTful api path /call/ner/entities
* AGDISTIS endpoint in `fox.properties` file
* server framework version update
* error pages
* fix server pool issue
* other minor changes

### [v2.2.1](https://github.com/AKSW/FOX/releases/tag/v2.2.1)
* installation update, because of an update of Illinois NER
* other minor changes

### [v2.2.0](https://github.com/AKSW/FOX/releases/tag/v2.2.0)
