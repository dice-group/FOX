[1]: ./examples/example.ttl
[2]: ./examples/example.json

# FOX API

## 1. Endpoints

### `/config` : configuration information

to find supported parameters for HTTP requests of the current instance, call  http://localhost:4444/config for a json file. E.g.:

```
{
  de: {
    ner: {
      SpotlightDE: "org.aksw.fox.tools.ner.de.SpotlightDE",
      StanfordDE: "org.aksw.fox.tools.ner.de.StanfordDE",
      BalieDE: "org.aksw.fox.tools.ner.de.BalieDE"
    },
      nerlinking: "org.aksw.fox.tools.linking.de.AgdistisDirectDE"
    },
  en: {
    ner: {
      StanfordEN: "org.aksw.fox.tools.ner.en.StanfordEN",
      BalieEN: "org.aksw.fox.tools.ner.en.BalieEN",
      OpenNLPEN: "org.aksw.fox.tools.ner.en.OpenNLPEN",
      IllinoisExtendedEN: "org.aksw.fox.tools.ner.en.IllinoisExtendedEN"
    },
    nerlinking: "org.aksw.fox.tools.linking.en.AgdistisDirectEN"
  },
  lang: [
    "de",
    "en"
  ],
  out: [
    "RDF/XML",
    "Turtle",
    "RDF/JSON",
    "JSON-LD",
    "TriG",
    "N-Quads"
  ]
}
```

### `/fox`: API

FOX accepts HTTP POST requests with content type `application/x-turtle; charset=utf-8` or `application/json; charset=utf-8` only.


The default endpoint is: http://localhost:4444/fox

### content type in x-turtle:

Send parameter with the header or with URL queries.

#### example requests with curl:
Sends a request with NIF input for the NER task only, for language DE and the light version of FOX (using just the given tool for the task) in this case with the StanfordDE wrapper.

Wihtout the `foxlight` parameter, FOX applies the ensemble learning with a pre trained multilayer perceptron.

```
curl -d "@example.ttl" -H "Content-Type:application/x-turtle;charset=utf-8" -H 'task:ner' http://localhost:4444/fox?lang=de&foxlight=org.aksw.fox.tools.ner.de.StanfordDE
```

example input file  [`example.ttl`][1]:

```
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .

<http://www.example.org/sentence-1#char=0,176>
  a               nif:Context ;
  nif:beginIndex  "0"^^xsd:nonNegativeInteger ;
  nif:endIndex    "176"^^xsd:nonNegativeInteger ;
  nif:isString    "Angela Dorothea Merkel geboren in Deutschland, ist eine deutsche Politikerin (CDU) und seit dem 22. November 2005 die amtierende Bundeskanzlerin der Bundesrepublik Deutschland."^^xsd:string .
```

### content type in json:

Send parameter in json format.

#### example requests with curl:

```
curl -d "@example.json" -H "Content-Type:application/json;charset=utf-8" http://localhost:4444/fox
```

example input file [`example.json`][2]:
```
{
"input" : "The philosopher and mathematician Leibniz was born in Leipzig in 1646 and attended the University of Leipzig from 1661-1666.",
"type": "text",
"task": "ner",
"output": "jsonld",
"lang": "en",
"foxlight":"org.aksw.fox.tools.ner.en.IllinoisExtendedEN"
}
```


## 2. Parameter

Find supported parameters for HTTP requests of the current instance, call  http://localhost:4444/config for a json file.


`input` : text or a url (for json content type only)

`type` : { `text` | `url` } (for json content type only)

`task` : { `ner` | `re`  }

`output` : { `JSON-LD` | `N-Triples` | `RDF/`{ `JSON` | `XML` } | `Turtle` | `TriG` | `N-Quads`}

`foxlight` : `org.aksw.fox.tools.ner.en.StanfordEN`  (a ner class name or `OFF`)

`lang` :  `de`, `en`, `es`, `fr`, `nl` (the supported languages)
``
