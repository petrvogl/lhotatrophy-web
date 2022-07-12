#!/bin/sh

# BUILD SCRIPT

SCRIPT_DIR=$(dirname $(readlink -f $0))
(
	GOALS=$(test "$1" = 'b' || echo 'clean ')"install"
	echo "goals: ${GOALS}"
	
	cd "${SCRIPT_DIR}/../" && \
		MAVEN_OPTS="-Xmx1g -XX:MaxMetaspaceSize=512m" \
		mvn ${GOALS} -Ptest -DskipTests=true
)
