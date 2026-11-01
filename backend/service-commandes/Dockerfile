FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml ./pom.xml
COPY service-commandes/pom.xml ./service-commandes/pom.xml
RUN mvn -B dependency:go-offline -f pom.xml || true

COPY service-commandes/src ./service-commandes/src
RUN mvn -B clean package -DskipTests -pl service-commandes -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
COPY --from=build /app/service-commandes/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]

