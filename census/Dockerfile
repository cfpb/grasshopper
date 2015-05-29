# Grasshopper Census Dockerfile
# Version: 0.0.1

# Image builds from the official Docker Java image

FROM java:8

MAINTAINER Juan Marin Otero <juan.marin.otero@gmail.com>

WORKDIR /

USER daemon

ENTRYPOINT ["java", "-jar", "/opt/grasshopper-census.jar"]

EXPOSE 8082

COPY target/scala-2.11/grasshopper-census.jar /opt/grasshopper-census.jar


