#!/bin/bash

# starts spotlight
java -Xmx4G -Dfile.encoding=utf-8 -jar dbpedia-spotlight-0.7.1.jar de http://localhost:4449/rest &
#nohup java -Xmx8G -Dfile.encoding=utf-8 -jar dbpedia-spotlight-0.7.1.jar en_2+2 http://localhost:4448/rest > &
java -Xmx4G -Dfile.encoding=utf-8 -jar dbpedia-spotlight-0.7.1.jar es http://localhost:4447/rest &
java -Xmx4G -Dfile.encoding=utf-8 -jar dbpedia-spotlight-0.7.1.jar fr http://localhost:4446/rest &
java -Xmx4G -Dfile.encoding=utf-8 -jar dbpedia-spotlight-0.7.1.jar nl http://localhost:4445/rest &
wait -n
exit 0
