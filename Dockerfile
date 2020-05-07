# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#
FROM openjdk:11 AS builder

# COPY . fineract is slow e.g. when using Podman instead of Docker (because it doesn't honor .dockerignore and copies all of .git/** into the container..), so let's explicitly list only what we need:
COPY fineract-provider/src/main fineract/fineract-provider/src/main/
COPY fineract-provider/config fineract/fineract-provider/config/
COPY fineract-provider/gradle fineract/fineract-provider/gradle/
COPY fineract-provider/properties fineract/fineract-provider/properties/
COPY fineract-provider/[bd]*.gradle fineract/fineract-provider/
COPY fineract-provider/gradle.properties fineract/fineract-provider/
COPY fineract-provider/gradlew fineract/fineract-provider/
COPY gradle* fineract/
COPY settings.gradle fineract/
COPY licenses fineract/licenses/
COPY *LICENSE* fineract/
COPY *NOTICE* fineract/

WORKDIR fineract
# RUN find .
RUN ./gradlew clean -x rat -x test war

# =========================================

FROM bitnami/tomcat:9.0 as fineract

USER root
RUN apt-get update -qq && apt-get install -y wget

COPY --from=builder /fineract/build/libs/fineract-provider.war /opt/bitnami/tomcat/webapps

RUN keytool -genkey -keyalg RSA -alias tomcat -keystore /opt/bitnami/tomcat/tomcat.keystore -keypass xyz123 -storepass xyz123 -noprompt -dname "CN=Fineract, OU=Fineract, O=Fineract, L=Unknown, ST=Unknown, C=Unknown"
COPY ./docker/server.xml /opt/bitnami/tomcat/conf
RUN chmod 664 /opt/bitnami/tomcat/conf/server.xml

WORKDIR /opt/bitnami/tomcat/lib
# org.drizzle.jdbc.DrizzleDriver is used by default for both the all tenants and demo tenant DB DataSource
RUN wget https://repo1.maven.org/maven2/org/drizzle/jdbc/drizzle-jdbc/1.4/drizzle-jdbc-1.4.jar

# https://issues.apache.org/jira/browse/LEGAL-462
# https://issues.apache.org/jira/browse/FINERACT-762
# We include an alternative JDBC driver (which is faster, but not allowed to be default in Apache distribution)
# allowing implementations to switch the driver used by changing start-up parameters (for both tenants and each tenant DB)
# The commented out lines in the docker-compose.yml illustrate how to do this.
# To be sure that this instead of Drizlle is used, comment out wget above.
RUN wget https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.19/mysql-connector-java-8.0.19.jar
