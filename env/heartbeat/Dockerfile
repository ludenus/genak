FROM ubuntu:18.04

RUN apt-get update && apt-get install -y curl

ADD heartbeat.sh /heartbeat.sh

ENV INFLUX_DB_URL="http://influxdb:8086"

ENV INFLUX_DB_NAME="qadb"

ENTRYPOINT /heartbeat.sh