#!/bin/bash

export USER=`whoami`
export HOST=`hostname`

env|sort

function send_data(){
    data="heartbeat,host=$HOST,user=$USER value=$((1 + RANDOM % 100)) $(date +%s000000001)"
    echo $data
    curl -sw '%{http_code} %{url_effective}\n' -XPOST "${INFLUX_DB_URL}/write?db=${INFLUX_DB_NAME}" --data-binary "$data"
}

for (( ; ; ))
do
   send_data
   sleep 10
done