#!/bin/bash

function beat(){
    data="heartbeat,host=`hostname`,user=`whoami` value=$((1 + RANDOM % 100)) $(date +%s000000001)"
    echo $data
    curl -i -XPOST 'http://localhost:8186/write?db=qadb' --data-binary "$data"
}

for (( ; ; ))
do
   beat
   sleep 1
done