#!/bin/bash

cd /usr/bin/FOX/spotlight

./runDE.sh 
./runEN.sh 
./runES.sh 
./runFR.sh 
./runNL.sh 

cd /usr/bin/FOX

mvn exec:java  -Dexec.mainClass="org.aksw.fox.ui.FoxRESTful"