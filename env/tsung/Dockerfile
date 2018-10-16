FROM ubuntu:18.04

ENV TSUNG_VERSION="1.7.0"
ENV TSUNG_XML="/root/.tsung/tsung.xml"

RUN apt-get update && apt-get install -y erlang wget make
RUN wget http://tsung.erlang-projects.org/dist/tsung-${TSUNG_VERSION}.tar.gz
RUN tar -xvf tsung-${TSUNG_VERSION}.tar.gz
RUN cd tsung-${TSUNG_VERSION} && ./configure && make && make install

COPY ./entrypoint.sh /entrypoint.sh
RUN chmod 755 /entrypoint.sh
ENTRYPOINT /entrypoint.sh