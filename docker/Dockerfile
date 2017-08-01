FROM maven:3.3.9-jdk-8

MAINTAINER R. Speck <speck@informatik.uni-leipzig.de>

# install all we need
RUN apt-get update && apt-get -y install \
    git \
    graphviz \
    && rm -rf /var/lib/apt/lists/*

# folder setup
RUN mkdir -p /usr/bin/FOX
WORKDIR /usr/bin/FOX

# environment setup
ENV MAVEN_OPTS "-Xmx20G -Dlog4j.configuration=file:/usr/bin/FOX/data/fox/log4j.properties -Dmaven.repo.local=/usr/bin/FOX/m2repository"

ENV DEBIAN_FRONTEND noninteractive

# get app and build
RUN cd /usr/bin/FOX \
        && git clone --branch master https://github.com/renespeck/FOX . \
        && mv fox.properties-dist fox.properties \
        && mvn clean compile -Dmaven.test.skip=true javadoc:javadoc

COPY run.sh /usr/bin/FOX/run.sh
RUN chmod +x /usr/bin/FOX/run.sh

#RUN ./downloadAgdistis.sh 
RUN ./downloadSpotlight.sh

# FOX port
EXPOSE 4444

# run app
# CMD mvn exec:java -Dexec.mainClass="org.aksw.fox.ui.FoxRESTful"
CMD ["/usr/bin/FOX/run.sh"]
