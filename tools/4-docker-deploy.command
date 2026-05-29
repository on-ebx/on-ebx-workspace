#!/bin/sh

workspace_loc="$(cd "$(dirname "${0}")" && pwd)"
maven_home="${workspace_loc}/apache-maven-3.6.3"
"${maven_home}/bin/mvn" -file "${workspace_loc}/../sling-mdm/pom.xml" -Pcsmus clean install
read -p "Enter docker repository name (and optional tag): "  repository
echo ${workspace_loc}
cd "${workspace_loc}"/..
docker login
docker build -f tools/Dockerfile -t $repository .

docker image ls

read -p "Enter overriding http port for running the docker image: "  httpport
read -p "Enter overriding https port for running the docker image: "  httpsport

read -p "Enter container name docker image: "  name
docker run --rm -p $httpport:9090 -p $httpsport:9443 --mount type=volume,src=$name,dst=/data/app/ebx --name $name $repository