# Set the base image to centos
FROM openjdk:8
###############################################################
# Dockerfile to build java container images
# Based on openjdk:8
# File Author / lidong
###############################################################
MAINTAINER lidong
ENV TZ=Asia/Shanghai
ENV ENV=dev
RUN mkdir -p /app
WORKDIR /app
ADD target/app.jar /app/
CMD java -jar /app/app.jar --server.port=8081

