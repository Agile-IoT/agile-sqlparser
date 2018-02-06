ARG BASEIMAGE_BUILD=agileiot/raspberry-pi3-zulujdk:8-jdk-maven
FROM $BASEIMAGE_BUILD

COPY SQLParser /usr/src/app
WORKDIR /usr/src/app

RUN ["mvn", "clean"]
RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]
RUN ["mvn", "package"]
#EXPOSE 8080
CMD ["mvn", "exec:java"]



