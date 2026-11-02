FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY pom.xml ../pom.xml
COPY eureka-server/pom.xml pom.xml
COPY eureka-server/src src

RUN ../mvnw clean package -DskipTests 2>/dev/null || true

FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
ARG JAR_FILE=eureka-server/target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
