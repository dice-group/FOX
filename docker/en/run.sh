#!/bin/bash

cd /usr/bin/FOX
mvn exec:java  -Dexec.mainClass="org.aksw.fox.ui.FoxRESTful"
