FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml ./pom.xml
COPY service-usagers/pom.xml ./service-usagers/pom.xml
RUN mvn -B dependency:go-offline -f pom.xml || true

COPY service-usagers/src ./service-usagers/src
RUN mvn -B clean package -DskipTests -pl service-usagers -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
COPY --from=build /app/service-usagers/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
