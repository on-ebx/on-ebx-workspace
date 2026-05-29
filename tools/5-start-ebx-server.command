#!/bin/sh



# This script starts the TIBCO EBX(TM) web app server for the "ebx-server" environment,
# following this procedure :
#
# 1. The script sets these environment variables :
#    JAVA_HOME, CATALINA_HOME, CATALINA_BASE, CATALINA_OPTS
#
# 2. The script sets the working directory :
#    cd <catalina base>
#
# 3. The script executes this command :
#    <catalina home>/bin/catalina.sh
#
# 4. The script terminates, showing this message :
#    ******** EBX(TM) stopped ********



main () {
  http_port=9090
  shutdown_port=9119

  checkPortUnallocated ${http_port} "HTTP"
  checkPortUnallocated ${shutdown_port} "SHUTDOWN"
  checkPortUnallocated ${jpda_port} "JPDA"

  workspace_loc="$(cd "$(dirname "${0}")" && pwd)"
  shared_lib_loc="${workspace_loc}/ebx-server/shared/lib"

  export JAVA_HOME="${workspace_loc}/tools/jdk8u252-b09/Contents/Home"
  export JAVA_OPTS="-Dhttp.port=${http_port} -Dshutdown.port=${shutdown_port}"
  export CATALINA_HOME="${workspace_loc}/tools/apache-tomcat"
  export CATALINA_BASE="${workspace_loc}/ebx-server"
  export CATALINA_OPTS="-Debx.home=\"${workspace_loc}/ebx-home\" -Debx.properties=\"${workspace_loc}/ebx-home/ebx.properties\" -Xmx2046M"

  cd "${CATALINA_BASE}"

  "${CATALINA_HOME}/bin/catalina.sh" run -config "conf/server.xml"

  echo
  echo
  echo "  ******** EBX(TM) stopped ********"
  echo
  echo
}



checkPortUnallocated () {
  port_number=${1}
  port_label=${2}

  netstat -an | grep "LISTEN" | grep "[.:]${port_number} " -q

  if [ $? -eq 0 ]; then
    echo
    echo
    echo "  ERROR: The ${port_number} ${port_label} port is already allocated to an existing process."
    echo
    echo
    exit 1
  fi
}



main "$@"
