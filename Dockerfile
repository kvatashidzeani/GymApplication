# Gym CRM (Main microservice)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Test-scoped dependency on trainer-workload-service requires it in the local Maven repo
COPY trainer-workload-service/pom.xml trainer-workload-service/pom.xml
COPY trainer-workload-service/src trainer-workload-service/src
RUN mvn -q -f trainer-workload-service/pom.xml -DskipTests install

COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/gym-crm-1.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["java", "-jar", "app.jar"]
