# Trainer Workload / Report microservice
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
# spring-boot-maven-plugin uses classifier=exec for the runnable fat jar
COPY --from=build /build/target/trainer-workload-service-1.0-SNAPSHOT-exec.jar app.jar
EXPOSE 8082
ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["java", "-jar", "app.jar"]
