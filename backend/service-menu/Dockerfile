FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml ./pom.xml
COPY service-menu/pom.xml ./service-menu/pom.xml
RUN mvn -B dependency:go-offline -f pom.xml || true

COPY service-menu/src ./service-menu/src
RUN mvn -B clean package -DskipTests -pl service-menu -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
COPY --from=build /app/service-menu/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
