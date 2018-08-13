[4]: http://fox.aksw.org
[6]: http://fox-demo.aksw.org

[7]: https://github.com/AKSW/FOX/releases/tag/v2.2.1
[8]: https://github.com/AKSW/FOX/releases/tag/v2.3.0
[9]: https://github.com/dice-group/FOX/tree/2.5.0

[![Build Status](https://travis-ci.org/dice-group/FOX.svg?branch=master)](https://travis-ci.org/dice-group/FOX)

## FOX - Federated Knowledge Extraction Framework
FOX ([http://fox.aksw.org][4]) is a framework that integrates the Linked Data Cloud and makes use of the diversity of NLP algorithms to extract RDF triples of high accuracy out of NL.
In its current version, it integrates and merges the results of Named Entity Recognition tools.
<!--Keyword Extraction and Relation Extraction tools will be merged soon.-->

## Requirements
Java 8, Maven 3, graphviz (for JavaDoc only)

## Documentation:
[documentation](documentation/readme.md).


## Demo
This version supports multiple languages for NER, NED and RE.

Live Demo: [http://fox-demo.aksw.org][6]

Release: [(Version 2.5.0)][9]

## How to cite

english version with details

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

extended version for multiple languages

```Tex
@InProceedings{speck2017,
   author={Ren{\'e} Speck and Axel-Cyrille {Ngonga Ngomo}},
   title={{Ensemble Learning of Named Entity Recognition Algorithms using Multilayer Perceptron for the Multilingual Web of Data}},
   booktitle={K-CAP 2017: Knowledge Capture Conference},
   year={2017},
   pages={4},
   organization={ACM},
   url={https://svn.aksw.org/papers/2017/KCAP_FOX/public.pdf},
 }
 ```

## License
FOX is licensed under the [GNU GPL Version 2, June 1991](LICENSE) (license document is in the application folder).

FOX uses several other libraries. An incomplete list is as follows:
* Illinois NLP Pipeline  (University of Illinois Research and Academic Use License)
* Stanford CoreNLP (GNU GPL Version 2)
* Apache OpenNLP (Apache License, Version 2)
* Balie (GNU GPL Version 2)

## Bugs
Found a :bug: bug? [Open an issue](https://github.com/AKSW/fox/issues/new) with some [emojis](http://emoji.muan.co). Issues without emojis are not valid. :trollface:
