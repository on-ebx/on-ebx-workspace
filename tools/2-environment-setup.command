#!/bin/sh



# This script installs the Maven projects of tools and workspaces,
# following this procedure :
#
# 1. The script sets these environment variables :
#    JAVA_HOME, JAVA_TOOL_OPTIONS
#
# 2. The script iterates over each "<psworkspace home>/tools/<tool>" directory.
#    If the directory contains a "maven/pom.xml" file,
#`   then the script executes this Maven command :
#    mvn -file <pom.xml path> clean install
#
# 3. The script iterates over each "<psworkspace home>/ebx-parent/<workspace>" directory.
#    If the directory contains a "pom.xml" file,
#`   then the script executes this Maven command :
#    mvn -file <pom.xml path> clean install
#
# 4. The script terminates, showing this message :
#    ******** EBX(TM) psworkspace installed ********


workspace_loc="$(cd "$(dirname "${0}")" && pwd)"
tools_loc="${workspace_loc}"
maven_home="${tools_loc}/apache-maven-3.6.3"

export JAVA_HOME="${tools_loc}/jdk8u252-b09/Contents/Home"
export JAVA_TOOL_OPTIONS="-Duser.home=\"${workspace_loc}/../\""

java -version

# JAVA_TOOL_OPTIONS is used instead of MAVEN_OPTS,
# in order to have quotes and spaces supported in options :
# See https://issues.apache.org/jira/browse/MNG-4559




# Using the -U parameter to force update from the nexus repository.
"${maven_home}/bin/mvn" -file "${workspace_loc}/../ebx-parent/pom.xml" -Pcsmus clean install -U


echo
echo
echo "  ******** EBX(TM) psworkspace installed ********"
echo
echo
