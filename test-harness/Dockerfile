# Grasshopper Test Harness Dockerfile
FROM java:8

MAINTAINER Hans Keeler <hans.keeler@cfpb.gov>

ENTRYPOINT ["java", "-jar", "/opt/grasshopper-test_harness.jar"]

COPY target/scala-2.11/grasshopper-test_harness.jar /opt/grasshopper-test_harness.jar
