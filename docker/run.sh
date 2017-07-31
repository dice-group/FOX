#!/bin/bash

cd spotlight
./runDE.sh 
./runEN.sh 
./runES.sh 
./runFR.sh 
./runNL.sh 
cd ..

mvn exec:java  -Dexec.mainClass="org.aksw.fox.ui.FoxRESTful"