#!/bin/sh

# SPOUSTECI SKRIPT PROJEKTU

SCRIPT_DIR=$(dirname $(readlink -f $0))
CONFIG_FILE="${SCRIPT_DIR}/start-app.properties"

######################################
# sekce konfigurace pro lokalni vyvoj
# k prepsani parametru staci tyto uvest do konfiguracniho souboru start-app.properties
DIST_DIR="${SCRIPT_DIR}/../target"
JAVA_HOME="${JAVA_HOME:=/opt/java}"
# paramtery pro JVM
JAVA_OPTS="-Xmx2g"
# parametry pro debug (test a lokalni vyvoj)
JAVA_DEBUG_ENABLED=n
JAVA_DEBUG_SUSPEND=n
JAVA_DEBUG_PORT=9111
# parametry pro spring hotswap
SPRING_DEVTOOLS_ENABLED=n
# aplikacni parametry (pro test)
APP_CONFIG_ENABLED=n
APP_CONFIG=
# soubor s buildem
JAR="lhotatrophy-web-0.0.1-SNAPSHOT.jar"
PROPERTY_FILE="${SCRIPT_DIR}/../conf/lhotatrophy.properties"
######################################


# spusteni
(
	cd "${SCRIPT_DIR}" || exit 1
	if [ -f "${CONFIG_FILE}" ]; then
		# vypis konfigurace
		echo "Config >>>"
		cat "${CONFIG_FILE}"
		echo "Config <<<"
		# nacteni konfigurace
		. "${CONFIG_FILE}"
	fi
	# FIX DCEVM - nefunguje s agentem, pozaduji pouze zakladni funkci pro hotswap
	if [ ! -z "$(${JAVA_HOME}/bin/java -version 2>/dev/null | grep -i dcevm)" ]; then
		JAVA_OPTS="${JAVA_OPTS} -XX:+DisableHotswapAgent"
	fi
	if [ "${SPRING_DEVTOOLS_ENABLED}" = "y" ]; then
		JAVA_OPTS="${JAVA_OPTS} -Dspring.devtools.restart.enabled=true"
	fi
	# zpracovani konfigurace
	if [ "${JAVA_DEBUG_ENABLED}" = "y" ]; then
		JAVA_DEBUG_OPTS="-agentlib:jdwp=transport=dt_socket,address=${JAVA_DEBUG_PORT},server=y,suspend=${JAVA_DEBUG_SUSPEND}"
	fi
	if [ "${APP_CONFIG_ENABLED}" != "y" ]; then
		APP_CONFIG=
	fi
	#echo \
	cd "${DIST_DIR}" || ( echo "Nelze vstoupit do adresare [${DIST_DIR}]"; exit 1 ) || exit 1
	#echo \
	WEB_LOCAL_CONFIG="${PROPERTY_FILE}"	\
	"${JAVA_HOME}/bin/java"		\
			${JAVA_OPTS}			\
			${JAVA_DEBUG_OPTS}		\
			-jar "${JAR}"			\
			${APP_CONFIG}			\
			$@
)
