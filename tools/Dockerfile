
FROM tomcat:8.5.57-jdk8-openjdk

ENV EBX_HOME /data/app/ebx
RUN mkdir -p ${EBX_HOME}

WORKDIR $CATALINA_HOME

COPY ebx-server/conf/context.xml ${CATALINA_HOME}/conf/
COPY ebx-server/conf/logging.properties ${CATALINA_HOME}/conf/
COPY ebx-server/conf/server-docker.xml $CATALINA_HOME/conf/server.xml
COPY ebx-server/conf/catalina.properties $CATALINA_HOME/conf/



#COPY ebx/*.jks $CATALINA_HOME/
COPY ebx-server/shared/lib/*.jar $CATALINA_HOME/lib/
COPY ebx-server/webapps/*.war $CATALINA_HOME/webapps/
COPY ebx-server/webapps/*.war $CATALINA_HOME/webapps/
COPY company-mdm/target/*.war $CATALINA_HOME/webapps/


COPY ebx-home/*.properties /data/app/
COPY company-mdm/properties/local/dev-artifacts.properties /data/app/

ENV EBX_OPTS="-Debx.home=${EBX_HOME} -Debx.properties=/data/app/ebx.properties -Ddev.artifacts.properties=/data/app/dev-artifacts.properties -DebxLicense=${EBXLICENSE}"

###
### startup parameters

ENV JAVA_OPTS="${EBX_OPTS} ${JAVA_OPTS}"
ENV CATALINA_OPTS ""

###
### SECURITY

RUN groupadd -g 1000 user \
   && useradd -u 1000 -g 1000 -m -s /bin/bash user \
   && chown -R 1000 /data /usr/local/tomcat
USER user

VOLUME ["/data/app/ebx"]

EXPOSE 8080
CMD ["catalina.sh", "run"]
