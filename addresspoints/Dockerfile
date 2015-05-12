# Grasshopper Addresspoints Dockerfile
# Version: 0.0.1

# Image builds from the official Docker Java image

FROM java:8

MAINTAINER Juan Marin Otero <juan.marin.otero@gmail.com>

WORKDIR /

USER daemon

COPY target/scala-2.11/grasshopper-addresspoints.jar /opt/grasshopper-addresspoints.jar

ENTRYPOINT ["java", "-jar", "/opt/grasshopper-addresspoints.jar"]

EXPOSE 8081


