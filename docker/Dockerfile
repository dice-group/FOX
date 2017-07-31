FROM maven:3.3.9-jdk-8

MAINTAINER R. Speck <renespeck@gmail.com>

ENV MAVEN_OPTS "-Xmx16G -Dlog4j.configuration=file:/FOX/data/fox/log4j.properties"
ENV DEBIAN_FRONTEND noninteractive

RUN git clone https://github.com/renespeck/FOX /FOX
WORKDIR /FOX

RUN chmod +x /FOX/downloadAgdistis.sh
RUN chmod +x /FOX/downloadSpotlight.sh
RUN chmod +x /FOX/ScriptCompile.sh

COPY run.sh /FOX/run.sh
RUN chmod +x /FOX/run.sh

RUN cp /FOX/fox.properties-dist /FOX/fox.properties
RUN /FOX/downloadAgdistis.sh
RUN /FOX/downloadSpotlight.sh

RUN mvn clean compile javadoc:javadoc -Dmaven.test.skip=false

EXPOSE 4444

CMD ["/FOX/run.sh"]
