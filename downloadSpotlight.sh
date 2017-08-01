#!/bin/bash

DIRECTORY="spotlight"
if [ ! -d "$DIRECTORY" ]; then
	echo "Downloading Spotlight data..."

	mkdir $DIRECTORY
	cd $DIRECTORY

	wget http://downloads.dbpedia-spotlight.org/spotlight/dbpedia-spotlight-0.7.1.jar
	# wget http://spotlight.sztaki.hu/downloads/dbpedia-spotlight-latest.jar 
	# wget http://spotlight.sztaki.hu/downloads/latest_models/de.tar.gz 
	# wget http://spotlight.sztaki.hu/downloads/latest_models/en.tar.gz 
	# wget http://spotlight.sztaki.hu/downloads/latest_models/es.tar.gz 
	# wget http://spotlight.sztaki.hu/downloads/latest_models/fr.tar.gz 
	# wget http://spotlight.sztaki.hu/downloads/latest_models/nl.tar.gz 
	wget http://downloads.dbpedia-spotlight.org/2016-04/de/model/de.tar.gz
	wget http://downloads.dbpedia-spotlight.org/2016-04/en/model/en_2%2b2.tar.gz
	wget http://downloads.dbpedia-spotlight.org/2016-04/es/model/es.tar.gz
	wget http://downloads.dbpedia-spotlight.org/2016-04/fr/model/fr.tar.gz
	wget http://downloads.dbpedia-spotlight.org/2016-04/nl/model/nl.tar.gz



	tar -xzf de.tar.gz 
	tar -xzf en_2+2.tar.gz
	tar -xzf es.tar.gz 
	tar -xzf fr.tar.gz 
	tar -xzf nl.tar.gz 
	
	rm de.tar.gz en_2+2.tar.gz  es.tar.gz fr.tar.gz nl.tar.gz 

	touch runDE.sh 

	echo "nohup java -Xmx4G -Dfile.encoding=utf-8 -jar dbpedia-spotlight-0.7.1.jar de http://localhost:4449/rest  > logDE.log &">> runDE.sh
	chmod +x runDE.sh 
	

	touch runEN.sh 
	echo "nohup java -Xmx8G -Dfile.encoding=utf-8 -jar dbpedia-spotlight-0.7.1.jar en_2+2 http://localhost:4448/rest  > logEN.log &">> runEN.sh
	chmod +x runEN.sh 
	
	touch runES.sh 
	echo "nohup java -Xmx4G -Dfile.encoding=utf-8 -jar dbpedia-spotlight-0.7.1.jar es http://localhost:4447/rest  > logES.log &">> runES.sh
	chmod +x runES.sh 
	
	touch runFR.sh 
	echo "nohup java -Xmx4G -Dfile.encoding=utf-8 -jar dbpedia-spotlight-0.7.1.jar fr http://localhost:4446/rest  > logFR.log &">> runFR.sh
	chmod +x runFR.sh 
	
	
	touch runNL.sh 
	echo "nohup java -Xmx4G -Dfile.encoding=utf-8 -jar dbpedia-spotlight-0.7.1.jar nl http://localhost:4445/rest  > logNL.log &">> runNL.sh
	chmod +x runNL.sh 


	echo "Downloaded Spotlight data."
	
	cd ..

fi