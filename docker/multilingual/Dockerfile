FROM maven:3-jdk-8

MAINTAINER R. Speck <rene.speck@uni-leipzig.de>

RUN apt-get update  && apt-get -y install \
    wget \
    unzip \
    git \
    graphviz \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /usr/bin/FOX_demo
WORKDIR /usr/bin/FOX_demo

ENV MAVEN_OPTS "-Xmx60G -Dlog4j.configuration=file:/usr/bin/FOX_demo/data/fox/log4j.properties"

ENV DEBIAN_FRONTEND noninteractive

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
RUN rm ./data/fox/cfg/org.aksw.fox.tools.ToolsGenerator.xml
RUN cp ./data/fox/cfg/org.aksw.fox.tools.ToolsGenerator.xml_default ./data/fox/cfg/org.aksw.fox.tools.ToolsGenerator.xml

RUN ./scripts/downloadSpotlight.sh

RUN mvn clean javadoc:javadoc compile -Dmaven.test.skip=true 

RUN unzip serial.zip -d tmp/ocelot

EXPOSE 4444 4445 4446 4447 4448 4449

RUN touch ./run.sh
RUN chmod +x ./run.sh
RUN echo "#!/bin/bash \n\n \
    # starts spotlight \n \
    cd /usr/bin/FOX_demo/spotlight \n\n \
    nohup java -Xmx4G -Dfile.encoding=utf-8 -jar dbpedia-spotlight-0.7.1.jar de https://localhost:4449/rest > /dev/null 2>&1 &  \n \
    nohup java -Xmx8G -Dfile.encoding=utf-8 -jar dbpedia-spotlight-0.7.1.jar en_2+2 https://localhost:4448/rest > /dev/null 2>&1 &  \n \
    nohup java -Xmx4G -Dfile.encoding=utf-8 -jar dbpedia-spotlight-0.7.1.jar es https://localhost:4447/rest > /dev/null 2>&1 &  \n \
    nohup java -Xmx4G -Dfile.encoding=utf-8 -jar dbpedia-spotlight-0.7.1.jar fr https://localhost:4446/rest > /dev/null 2>&1 &  \n \
    nohup java -Xmx4G -Dfile.encoding=utf-8 -jar dbpedia-spotlight-0.7.1.jar nl https://localhost:4445/rest > /dev/null 2>&1 &  \n \
    # starts FOX \n \
    cd /usr/bin/FOX_demo \n\n \
    mvn exec:java  -Dexec.mainClass=\"org.aksw.fox.ui.FoxRESTful\" " >  ./run.sh

CMD ["/usr/bin/FOX_demo/run.sh"]