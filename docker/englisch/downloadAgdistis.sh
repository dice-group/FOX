#!/bin/bash
# =======================================
DIRECTORY="./agdistis"
if [ ! -d "$DIRECTORY" ]; then
	#
	echo "Downloading Agdistis data..."
	#
	mkdir $DIRECTORY
	cd $DIRECTORY
	#
	# EN
	wget http://hobbitdata.informatik.uni-leipzig.de/agdistis/dbpedia_index_2016-04/en/indexdbpedia_en_2016.zip 
	# DE
	#wget http://hobbitdata.informatik.uni-leipzig.de/agdistis/dbpedia_index_2016-04/de/indexdbpedia_de_2016.zip  
	# FR
	#wget http://hobbitdata.informatik.uni-leipzig.de/agdistis/dbpedia_index_2016-04/fr/indexdbpedia_fr_2016.zip
	# ES
	#wget http://hobbitdata.informatik.uni-leipzig.de/agdistis/dbpedia_index_2016-04/es/indexdbpedia_es_2016.zip
	# NL
	#wget http://hobbitdata.informatik.uni-leipzig.de/agdistis/dbpedia_index_2016-04/nl/indexdbpedia_nl_2016.zip
	#
	#
	unzip indexdbpedia_en_2016.zip
	#unzip indexdbpedia_de_2016.zip
	#unzip indexdbpedia_fr_2016.zip
	#unzip indexdbpedia_es_2016.zip
	#unzip indexdbpedia_nl_2016.zip


	rm indexdbpedia_en_2016.zip
	#rm indexdbpedia_de_2016.zip
	#rm indexdbpedia_fr_2016.zip
	#rm indexdbpedia_es_2016.zip
	#rm indexdbpedia_nl_2016.zip

	cd ..
	echo "Downloaded Agdistis data." 
fi 
