# Dockerfile for universalresolver/driver-did-btc1

FROM maven:3-jdk-11 AS build
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build driver-did-btc1

ADD . /opt/driver-did-btc1
RUN cd /opt/driver-did-btc1 && mvn clean install package -P war -N -DskipTests


FROM jetty:9.4-jre11-slim
MAINTAINER Markus Sabadello <markus@danubetech.com>

# variables

ENV uniresolver_driver_did_btc1_bitcoinConnection=blockcypherapi
ENV uniresolver_driver_did_btc1_rpcUrlMainnet=http://user:pass@localhost:8332/
ENV uniresolver_driver_did_btc1_rpcUrlTestnet=http://user:pass@localhost:18332/
ENV uniresolver_driver_did_btc1_rpcCertMainnet=
ENV uniresolver_driver_did_btc1_rpcCertTestnet=
ENV uniresolver_driver_did_btc1_ipfs=/ip4/127.0.0.1/tcp/5001

# copy from build stage
COPY --from=build --chown=jetty /opt/driver-did-btc1/target/*.war /var/lib/jetty/webapps/ROOT.war

#done

EXPOSE 8080

CMD java -Djetty.http.port=8080 -Dorg.eclipse.jetty.annotations.maxWait=240 -jar /usr/local/jetty/start.jar
