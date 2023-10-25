#
# slim to clone dependencies
#
FROM debian:11-slim as slim
RUN apt-get update && apt-get install -y unzip wget git
#
# clones knowledgeextraction
#
FROM slim AS ke
RUN git clone --branch master https://github.com/renespeck/knowledgeextraction.git ke && cd ke && git checkout v0.0.2
#
# clones ocelot
#
FROM slim AS ocelot
RUN git clone --branch master https://github.com/dice-group/Ocelot.git oc && cd oc && git checkout v0.0.2
#
# clones fox
#
FROM slim AS fox
RUN git clone --branch master https://github.com/dice-group/FOX foxtmp
#
# builds knowledgeextraction
#
FROM maven:3.8.3-jdk-11-slim as builder_ke
RUN mkdir -p /usr/bin/FOX
WORKDIR /usr/bin/FOX
COPY --from=ke ke ./ke
RUN cd ke && mvn clean compile install test
RUN rm -R ke
#
# builds ocelot
#
FROM maven:3.8.3-jdk-11-slim as builder_oce
RUN mkdir -p /usr/bin/FOX
WORKDIR /usr/bin/FOX
COPY --from=ocelot oc ./oc
COPY --from=builder_ke /root/.m2 /root/.m2
RUN cd oc &&  mvn clean compile install -Dmaven.test.skip=true
RUN rm -R oc
#
# set up fox
#
FROM maven:3.8.3-jdk-11-slim  as builder_fox
RUN mkdir -p /usr/bin/FOX
WORKDIR /usr/bin/FOX
COPY --from=builder_oce /root/.m2 /root/.m2
RUN apt-get update  && apt-get -y install \
    unzip \
    graphviz \
    && rm -rf /var/lib/apt/lists/*

COPY --from=fox foxtmp ./fox
RUN cp ./fox/fox.properties-dist ./fox/fox.properties
RUN cp ./fox/data/fox/cfg/org.aksw.fox.tools.ToolsGenerator.xml_default ./fox/data/fox/cfg/org.aksw.fox.tools.ToolsGenerator.xml
#
# Workaround to get javadoc with java 8 since FOX produces a runtime error with 8 but not with Java 11, while 11 is not creating javadoc with UML. Feel free to fix this. ;)
#
FROM ubuntu:16.04 as javadoc
RUN apt-get update && \
    apt-get install -y openjdk-8-jdk && \
    apt-get install -y ant && \
    apt-get install -y git graphviz && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/ && \
    rm -rf /var/cache/oracle-jdk8-installer;

RUN mkdir -p /usr/bin/FOX
WORKDIR /usr/bin/FOX
COPY --from=builder_fox /usr/bin/FOX/fox ./javadoc
COPY --from=builder_fox /root/.m2 /root/.m2
RUN cd javadoc &&  mvn clean compile -Dmaven.test.skip=true javadoc:javadoc
#
# builds fox
#
FROM builder_fox
RUN cd fox &&  mvn clean compile -Dmaven.test.skip=true
RUN cd fox && unzip serial.zip -d tmp/ocelot

COPY --from=javadoc /usr/bin/FOX/javadoc/src/main/resources/public/demo/doc ./fox/release/classes/public/demo/doc

RUN cd ./fox && rm -R serial.zip src evaluation input .git
# RUN rm -R ke oc

EXPOSE 4444

RUN touch ./fox/run.sh
RUN chmod +x ./fox/run.sh
RUN echo "#!/bin/bash \n\n \
    # starts FOX \n \
    cd fox \n\n \
    mvn exec:java  -Dexec.mainClass=\"org.aksw.fox.ui.FoxRESTful\" " >  ./fox/run.sh
CMD ["fox/run.sh"]
