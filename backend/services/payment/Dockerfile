# syntax=docker/dockerfile:1
# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .

# --- THAY ĐỔI Ở ĐÂY ---
# 1. Xóa "-q" để hiện log (biết được tiến độ download).
# 2. Thêm "-B" (Batch mode) cho log gọn.
# 3. Thêm các cấu hình timeout và retry để chống rớt mạng.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -e -B -DskipTests \
    -Dmaven.wagon.http.connectTimeout=120000 \
    -Dmaven.wagon.http.readTimeout=120000 \
    -Dmaven.wagon.http.retryHandler.count=3 \
    dependency:go-offline

COPY src ./src

# Build package
RUN --mount=type=cache,target=/root/.m2 \
    mvn -e -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar /app/app.jar
ENV JAVA_OPTS=""
EXPOSE 8076

ENTRYPOINT ["java", "-jar", "/app/app.jar"] 
CMD ["$JAVA_OPTS"]