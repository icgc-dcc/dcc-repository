#!/bin/bash
#
# Copyright 2015(c) The Ontario Institute for Cancer Research. All rights reserved.
#
# Description:
#   Runs dcc-import-client
#
# Usage:
#   ./dcc-repository-client.sh <arguments>
#

# Prevent this var causing issues with Spring Boot's environment binding
unset MAIL;

base_dir=$(dirname $0)/..
java_opts="-Xmx4g"

java \
	${java_opts} \
  -Dlog.dir=${base_dir}/logs \
  -Dlogging.config=${base_dir}/conf/logback.xml \
  -Dspring.config.location=file:${base_dir}/conf/ \
  -Dcom.sun.management.jmxremote.port=10000 \
  -Dcom.sun.management.jmxremote.ssl=false \
  -Dcom.sun.management.jmxremote.authenticate=false \
	-jar ${base_dir}/lib/dcc-repository-client.jar "$@"