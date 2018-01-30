FROM java:8

# Install maven
RUN apt-get update
RUN apt-get install -y maven

COPY SQLParser /usr/src/app
WORKDIR /usr/src/app

RUN ["mvn", "clean"]
RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]
RUN ["mvn", "package"]
#EXPOSE 8080
CMD ["mvn", "exec:java"]



