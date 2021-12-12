[4]: https://dice-research.org/FOX
[6]: https://fox.demos.dice-research.org/

[![Build Status](https://travis-ci.org/dice-group/FOX.svg?branch=master)](https://travis-ci.org/dice-group/FOX)
[![BCH compliance](https://bettercodehub.com/edge/badge/dice-group/FOX?branch=master)](https://bettercodehub.com/)
[![Project Stats](https://www.openhub.net/p/FOX-Framework/widgets/project_thin_badge.gif)](https://www.openhub.net/p/FOX-Framework)
<!---
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/348e14317ea140cbb98a110c40718d88)](https://www.codacy.com/app/renespeck/FOX?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dice-group/FOX&amp;utm_campaign=Badge_Grade)
-->

## FOX - Federated Knowledge Extraction Framework
FOX ([https://dice-research.org/FOX][4]) is a framework that integrates the Linked Data Cloud and makes use of the diversity of NLP algorithms to extract RDF triples of high accuracy out of NL.

In its current version, it integrates and merges the results of Named Entity Recognition tools as well as it integrates several Relation Extraction tools.

## Requirements
Java 8, Maven 3, graphviz (for JavaDoc only)

## Documentation:
[documentation](documentation/readme.md).

## Demo
This version supports multiple languages for NER, NED and RE.

Live Demo: [https://fox.demos.dice-research.org/][6]

## How to cite

English version with details:

```Tex
@incollection{
  year={2014},
  isbn={978-3-319-11963-2},
  booktitle={The Semantic Web – ISWC 2014},
  volume={8796},
  series={Lecture Notes in Computer Science},
  title={Ensemble Learning for Named Entity Recognition},
  publisher={Springer International Publishing},
  author={Ren{\'e} Speck and Axel-Cyrille {Ngonga Ngomo}},
}
```

The extended version for multiple languages:

```Tex
@InProceedings{speck2017,
   author={Ren{\'e} Speck and Axel-Cyrille {Ngonga Ngomo}},
   title={{Ensemble Learning of Named Entity Recognition Algorithms using Multilayer Perceptron for the Multilingual Web of Data}},
   booktitle={K-CAP 2017: Knowledge Capture Conference},
   year={2017},
   pages={4},
   organization={ACM}
 }
 ```

## License

FOX is licensed under the [GNU Affero General Public License v3.0](LICENSE) (license document is in the application folder).

FOX uses several other libraries. An incomplete list is as follows:
* Illinois NLP Pipeline  (University of Illinois Research and Academic Use License)
* Stanford CoreNLP (GNU GPL Version 2)
* Apache OpenNLP (Apache License, Version 2)
* Balie (GNU GPL Version 2)


## Bugs
Found a :bug: bug? [Open an issue](https://github.com/dice-group/FOX/issues/new) with some [emojis](http://emoji.muan.co). Issues without emojis are not valid. :trollface:
