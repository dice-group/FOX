#!/bin/bash

# nohup mvn clean compile javadoc:javadoc -Dmaven.test.skip=false test  > logCompile.log &
nohup mvn clean compile -Dmaven.test.skip=true  > logCompile.log &