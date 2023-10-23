#
#
#
# slim to clone dependencies
FROM debian:11-slim as slim
RUN apt-get update && apt-get install -y unzip wget git
#
#
# clones knowledgeextraction
FROM slim AS ke
RUN git clone --branch master https://github.com/renespeck/knowledgeextraction.git ke && cd ke && git checkout v0.0.2
#
#
#
# clones ocelot
FROM slim AS ocelot
RUN git clone --branch master https://github.com/dice-group/Ocelot.git oc && cd oc && git checkout v0.0.2
#
#
#
FROM slim AS fox
RUN git clone --branch master https://github.com/dice-group/FOX foxtmp
#
#
#
FROM maven:sapmachine as builder_ke
COPY --from=ke ke ./ke
RUN cd ke && mvn clean compile install test
#
#
#
FROM builder_ke as builder_oce
COPY --from=ocelot oc ./oc
RUN cd oc &&  mvn clean compile install -Dmaven.test.skip=true
#
#
#
FROM builder_oce as builder_fox
RUN apt-get update  && apt-get -y install \
    unzip \
    graphviz \
    && rm -rf /var/lib/apt/lists/*

COPY --from=fox foxtmp ./fox
RUN cp ./fox/fox.properties-dist ./fox/fox.properties
RUN cp ./fox/data/fox/cfg/org.aksw.fox.tools.ToolsGenerator.xml_default ./fox/data/fox/cfg/org.aksw.fox.tools.ToolsGenerator.xml
RUN cd fox &&  mvn clean compile javadoc:javadoc -Dmaven.test.skip=true

RUN cd fox && unzip serial.zip -d tmp/ocelot

EXPOSE 4444

RUN touch ./fox/run.sh
RUN chmod +x ./fox/run.sh
RUN echo "#!/bin/bash \n\n \
    # starts FOX \n \
    cd fox \n\n \
    mvn exec:java  -Dexec.mainClass=\"org.aksw.fox.ui.FoxRESTful\" " >  ./fox/run.sh

CMD ["fox/run.sh"]
