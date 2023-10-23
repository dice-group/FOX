# slim to clone dependencies
FROM debian:11-slim as slim
RUN apt-get update && apt-get install -y unzip wget git
#
# clones knowledgeextraction
FROM slim AS spotlightjar
RUN wget https://sourceforge.net/projects/dbpedia-spotlight/files/spotlight/dbpedia-spotlight-0.7.1.jar
FROM slim AS spotlightde
RUN wget https://sourceforge.net/projects/dbpedia-spotlight/files/2016-04/de/model/de.tar.gz && tar -xzf de.tar.gz
FROM slim AS spotlighten
RUN wget  https://sourceforge.net/projects/dbpedia-spotlight/files/2016-04/en/model/en_2%2b2.tar.gz && tar -xzf en_2+2.tar.gz
FROM slim AS spotlightes
RUN wget  https://sourceforge.net/projects/dbpedia-spotlight/files/2016-04/es/model/es.tar.gz && tar -xzf es.tar.gz
FROM slim AS spotlightfr
RUN wget  https://sourceforge.net/projects/dbpedia-spotlight/files/2016-04/fr/model/fr.tar.gz && tar -xzf fr.tar.gz
FROM slim AS spotlightnl
RUN wget  https://sourceforge.net/projects/dbpedia-spotlight/files/2016-04/nl/model/nl.tar.gz && tar -xzf nl.tar.gz


FROM maven:3-jdk-8 AS base
COPY --from=spotlightjar dbpedia-spotlight-0.7.1.jar ./dbpedia-spotlight-0.7.1.jar
COPY --from=spotlightde de ./de
COPY --from=spotlighten en_2+2 ./en_2+2
COPY --from=spotlightes es ./es
COPY --from=spotlightfr fr ./fr
COPY --from=spotlightnl nl ./nl

EXPOSE 4445 4446 4447 4448 4449

# curl -v -d "text=Obama USA." -H "Accept:application/json" http://127.0.0.1:4449/rest/annotate
COPY run.sh ./run.sh
RUN chmod +x ./run.sh
CMD ["./run.sh"]
