#!/bin/sh



# This script stops the TIBCO EBX(TM) web app server for the "ebx-servert" environment,
# following this procedure :
#
# 1. The script sets these environment variables :
#    JAVA_HOME, CATALINA_HOME, CATALINA_BASE
#
# 2. The script sets the working directory :
#    cd <catalina base>
#
# 3. The script executes this command :
#    <catalina home>/bin/shutdown.sh
#
# 4. The script terminates, showing this message :
#    ******** EBX(TM) stopped ********



main () {
  shutdown_port=9119

  checkPortAllocated ${shutdown_port} "SHUTDOWN"

  workspace_loc="$(cd "$(dirname "${0}")" && pwd)"
  workspace_loc="${workspace_loc}"

  export JAVA_HOME="${workspace_loc}/tools/jdk8u252-b09/Contents/Home"
  export JAVA_OPTS="-Dshutdown.port=${shutdown_port}"
  export CATALINA_HOME="${workspace_loc}/tools/apache-tomcat"
  export CATALINA_BASE="${workspace_loc}/ebx-server"

  cd "${CATALINA_BASE}"

  "${CATALINA_HOME}/bin/shutdown.sh"

  echo
  echo
  echo "  ******** EBX(TM) stopped ********"
  echo
  echo
}



checkPortAllocated () {
  port_number=${1}
  port_label=${2}

  netstat -an | grep "LISTEN" | grep "[.:]${port_number} " -q

  if [ $? -ne 0 ]; then
    echo
    echo
    echo "  ERROR: The ${port_number} ${port_label} port is not allocated to any process."
    echo
    echo
    exit 1
  fi
}



main "$@"
