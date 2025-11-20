# syntax=docker/dockerfile:1
# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
# Note: Using the official Maven repository only is generally cleaner unless internal proxy is required
# We keep the original for consistency but simplify the RUN command
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar /app/app.jar
ENV JAVA_OPTS=""
EXPOSE 8074

# FIX: Use the exec form for ENTRYPOINT to ensure the Java process 
# is the main process (PID 1) and receives the SIGTERM signal from Docker.
# This allows the Spring Boot application to run its shutdown hook 
# and deregister cleanly from Eureka.
ENTRYPOINT ["java", "-jar", "/app/app.jar"] 
CMD ["$JAVA_OPTS"]
