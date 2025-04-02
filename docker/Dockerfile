# Dockerfile for universalresolver/driver-did-btc1

FROM maven:3-eclipse-temurin-21-alpine AS build
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build driver-did-btc1

ADD pom.xml /opt/driver-did-btc1/
RUN cd /opt/driver-did-btc1/ && mvn org.apache.maven.plugins:maven-dependency-plugin:3.8.1:go-offline
ADD . /opt/driver-did-btc1/
RUN cd /opt/driver-did-btc1/ && mvn clean package -P war -N -DskipTests


FROM jetty:12.0.18-jre21-eclipse-temurin
MAINTAINER Markus Sabadello <markus@danubetech.com>

# variables

ENV uniresolver_driver_did_btc1_bitcoinConnection=bitcoind
ENV uniresolver_driver_did_btc1_rpcUrlMainnet=http://user:pass@localhost:8332/
ENV uniresolver_driver_did_btc1_rpcUrlTestnet=http://user:pass@localhost:18332/
ENV uniresolver_driver_did_btc1_rpcUrlSignet=
ENV uniresolver_driver_did_btc1_rpcUrlRegtest=
ENV uniresolver_driver_did_btc1_rpcCertMainnet=
ENV uniresolver_driver_did_btc1_rpcCertTestnet=
ENV uniresolver_driver_did_btc1_rpcCertSignet=
ENV uniresolver_driver_did_btc1_rpcCertRegtest=
ENV uniresolver_driver_did_btc1_ipfs=/ip4/127.0.0.1/tcp/5001

# copy from build stage

COPY --from=build --chown=jetty /opt/driver-did-btc1/target/*.war /var/lib/jetty/webapps/ROOT.war

#done

EXPOSE 8080
RUN java -Djetty.http.port=8080 -jar "$JETTY_HOME/start.jar" --add-modules=ee10-webapp,ee10-deploy,ee10-jsp,ee10-jstl
CMD java -Djetty.http.port=8080 -jar "$JETTY_HOME/start.jar"
