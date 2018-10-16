#!/bin/bash
set -x
set -e

env | sort

tsung -v

tsung -f ${TSUNG_XML} start
