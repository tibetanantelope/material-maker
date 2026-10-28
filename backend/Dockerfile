FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY pom.xml .
COPY backend/pom.xml backend/pom.xml
RUN mvn -q -pl backend -am dependency:go-offline
COPY backend/src backend/src
RUN mvn -q -pl backend -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN mkdir -p /app/runtime/uploads
COPY --from=builder /workspace/backend/target/material-maker-backend-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
