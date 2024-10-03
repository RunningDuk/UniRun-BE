FROM openjdk:17-jdk-slim
WORKDIR /unirun-api-server
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]