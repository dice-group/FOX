FROM maven:3-jdk-8

MAINTAINER R. Speck <rene.speck@uni-leipzig.de>

# install all we need
RUN apt-get update && apt-get -y install \
    wget \
    unzip \
    git \
    graphviz \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /usr/bin/FOX_demo
WORKDIR /usr/bin/FOX_demo

# environment setup
# ENV MAVEN_OPTS "-Xmx20G -Dlog4j.configuration=file:/usr/bin/FOX_demo/data/fox/log4j.properties -Dmaven.repo.local=/usr/bin/FOX_demo/repository"

ENV MAVEN_OPTS "-Xmx20G -Dlog4j.configuration=file:/usr/bin/FOX_demo/data/fox/log4j.properties"

ENV DEBIAN_FRONTEND noninteractive

# get app 
RUN git clone --branch master https://github.com/renespeck/knowledgeextraction.git ke
RUN cd ke && git checkout v0.0.2 && mvn compile install
RUN rm -fr ke

RUN git clone --branch master https://github.com/dice-group/Ocelot.git oc
RUN cd oc && git checkout v0.0.2 && mvn -Dmaven.test.skip=true compile install
RUN rm -fr oc

RUN git clone --branch master https://github.com/dice-group/FOX foxtmp
RUN cp -r foxtmp/* ./
RUN rm -R foxtmp

RUN cp ./fox.properties-dist ./fox.properties
# COPY org.aksw.fox.tools.ToolsGenerator.xml ./data/fox/cfg/
RUN mvn clean javadoc:javadoc compile -Dmaven.test.skip=true 

RUN unzip serial.zip -d tmp/ocelot

# FOX port
EXPOSE 4444

CMD mvn exec:java  -Dexec.mainClass="org.aksw.fox.ui.FoxRESTful" 

