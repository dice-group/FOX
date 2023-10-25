# slim to clone dependencies
FROM debian:11-slim as slim
RUN apt-get update && apt-get install -y unzip wget
# folder setup
RUN mkdir -p /usr/bin/FOX
WORKDIR /usr/bin/FOX
#
#
# gets spotlight and models
FROM slim AS spotlightjar
RUN wget https://sourceforge.net/projects/dbpedia-spotlight/files/spotlight/dbpedia-spotlight-0.7.1.jar
# de
FROM slim AS spotlightde
RUN wget https://sourceforge.net/projects/dbpedia-spotlight/files/2016-04/de/model/de.tar.gz && tar -xzf de.tar.gz
# en
#FROM slim AS spotlighten
#RUN wget  https://sourceforge.net/projects/dbpedia-spotlight/files/2016-04/en/model/en_2%2b2.tar.gz && tar -xzf en_2+2.tar.gz
# es
FROM slim AS spotlightes
RUN wget  https://sourceforge.net/projects/dbpedia-spotlight/files/2016-04/es/model/es.tar.gz && tar -xzf es.tar.gz
# fr
FROM slim AS spotlightfr
RUN wget  https://sourceforge.net/projects/dbpedia-spotlight/files/2016-04/fr/model/fr.tar.gz && tar -xzf fr.tar.gz
# nl
FROM slim AS spotlightnl
RUN wget  https://sourceforge.net/projects/dbpedia-spotlight/files/2016-04/nl/model/nl.tar.gz && tar -xzf nl.tar.gz
#
#
#
FROM adoptopenjdk/openjdk8:x86_64-ubuntu-jre8u-2023-10-23-22-01-beta-nightly AS base
# folder setup
RUN mkdir -p /usr/bin/FOX
WORKDIR /usr/bin/FOX
RUN cd /usr/bin/FOX
#
COPY --from=spotlightjar /usr/bin/FOX/dbpedia-spotlight-0.7.1.jar ./dbpedia-spotlight-0.7.1.jar
COPY --from=spotlightde /usr/bin/FOX/de ./de
#COPY --from=spotlighten /usr/bin/FOX/en_2+2 ./en_2+2
COPY --from=spotlightes /usr/bin/FOX/es ./es
COPY --from=spotlightfr /usr/bin/FOX/fr ./fr
COPY --from=spotlightnl /usr/bin/FOX/nl ./nl

#EXPOSE 4445 4446 4447 4448 4449
EXPOSE 4445 4446 4447 4449

# curl -v -d "text=Obama USA." -H "Accept:application/json" http://127.0.0.1:4449/rest/annotate
COPY run.sh ./run.sh
RUN chmod +x ./run.sh
CMD ["./run.sh"]
