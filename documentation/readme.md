[8]: https://github.com/AKSW/FOX/releases/tag/v2.3.0
[9]: https://github.com/AKSW/FOX
[7]: ./example.ttl
[6]: ./requirements.md

## New Version ([2.4.0][9])


### requirements

java 8, maven, wget, unzip, tar, <docker>


### Requests


Example input file: [example.ttl][7]

using FOX with a trained MLP with NER and RE in English:

`curl -d "@example.ttl" -H "Content-Type:application/x-turtle; charset=utf-8"  http://fox.cs.uni-paderborn.de:4444/fox?task=re&lang=en> response.txt`

using the light version of NER (means in this case the Stanford CoreNLP tools only) in English

`curl -d "@example.ttl" -H "Content-Type:application/x-turtle; charset=utf-8"  http://fox.cs.uni-paderborn.de:4444/fox?task=ner&lang=de&foxlight=org.aksw.fox.tools.ner.de.StanfordDE > response.txt`


### Build:

the docker file is in the `Docker` folder


## Old Version ([2.3.0][8])

### Build:

Copy `fox.properties-dist` to `fox.properties` and run `./build.sh`.

Now, the release is ready in the `release` folder, `cd release`.

### Run:

Copy `fox.properties-dist` to `fox.properties` and run `run.sh`  to start the server.

To close the server, run `close.sh`.
