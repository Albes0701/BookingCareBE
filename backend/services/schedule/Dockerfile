# syntax=docker/dockerfile:1

# Dependencies stage (shared base)
FROM maven:3.9.9-eclipse-temurin-21 AS dependencies
WORKDIR /workspace
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -DskipTests dependency:go-offline

# Build stage
FROM dependencies AS build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

# Development stage  
FROM dependencies AS development
COPY src ./src
EXPOSE 8077
CMD ["mvn", "spring-boot:run", "-Dspring.devtools.restart.enabled=true", "-Dspring.devtools.livereload.enabled=true"]

# Production stage
FROM eclipse-temurin:21-jre AS production
WORKDIR /app
COPY --from=build /workspace/target/*.jar /app/app.jar
ENV JAVA_OPTS=""
EXPOSE 8077
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]