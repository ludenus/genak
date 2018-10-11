#!/bin/bash

data="cpu_load_short,host=server01,region=us-west value=$((1 + RANDOM % 100)) $(date +%s000000001)"
echo $data
curl -i -XPOST 'http://localhost:8186/write?db=qadb' --data-binary "$data"
