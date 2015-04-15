# Addresspoints Dockerfile
# Version: 0.0.1

# Image builds from the official Docker Java image

FROM java:8

MAINTAINER Juan Marin Otero <juan.marin.otero@gmail.com>

WORKDIR /

USER daemon

ADD target/scala-2.11/addresspoints-assembly-0.0.1.jar /opt/addresspoints.jar

ENTRYPOINT ["java", "-jar", "/opt/addresspoints.jar"]

EXPOSE 8080


