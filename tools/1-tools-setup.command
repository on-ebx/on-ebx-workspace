#!/bin/bash

# download and install
# apache-tomcat-8.5.60
# maven 3.6.3
# download openjdk8

##### curl variant:
## • -C   OR   --continue-at          ==> continuation from the last download, if any. And -C - for auto-detection.
## • -L   OR   --location             ==> follow requested page, if moved to a different location.
## • -O   OR   --remote-name          ==> name the local file same as remote file name.
## • -#   OR   --progress-bar         ==> displays transfer progress as a simple progress bar. [not to be used with -s]
## • -H   OR   --header               ==> header/@file to be included in the request.
## • -R   OR   --remote-time          ==> local file will get the remote file timestamp.
## • -s   OR   --silent               ==> quiet mode.
## • -S   OR   --show-error           ==> show errors, if any.

#source variables.command



if [ ! -d "apache-tomcat" ]; then
  echo "apache-tomcat $MSG_NOT_FOUND"
  url=https://ftp.wayne.edu/apache/tomcat/tomcat-8/v8.5.60/bin/apache-tomcat-8.5.60.zip
  


  echo "getting $url..."
  curl -LROH -k "$url"
  unzip apache-tomcat-8.5.60.zip
  mv apache-tomcat-8.5.60 apache-tomcat
  rm -rf apache-tomcat-8.5.60
  chmod +x apache-tomcat/bin/catalina.sh apache-tomcat/bin/setclasspath.sh apache-tomcat/bin/shutdown.sh
  rm apache-tomcat-8.5.60.zip
  echo ""
else
  echo "apache-tomcat $MSG_OK"
fi


if [ ! -d "apache-maven-3.6.3" ]; then
  echo "apache-maven-3.6.3 $MSG_NOT_FOUND"
  url=https://ftp.wayne.edu/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.zip
  echo "getting $url..."
  curl -LROH -k "$url"
  unzip apache-maven-3.6.3-bin.zip
  rm apache-maven-3.6.3-bin.zip
  echo ""
else
  echo "apache-maven-3.6.3 $MSG_OK"
fi



if [ ! -d "jdk8u252-b09" ]; then
  echo "jdk8u252-b09 $MSG_NOT_FOUND"
  url=https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u252-b09.1/OpenJDK8U-jdk_x64_mac_hotspot_8u252b09.tar.gz
  echo "getting $url..."
  curl -LROH -k "$url"
  tar -xf OpenJDK8U-jdk_x64_mac_hotspot_8u252b09.tar.gz
  rm OpenJDK8U-jdk_x64_mac_hotspot_8u252b09.tar.gz
  echo ""
else
  echo "jdk8u252-b09 $MSG_OK"
fi
