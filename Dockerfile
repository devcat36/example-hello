# syntax=docker/dockerfile:1
# Multi-stage build. Built multi-arch (linux/amd64 + linux/arm64) so the same
# tag runs on the amd64 nodes (main, us-east) and the arm64 Jetson (pg).
FROM gradle:8.13-jdk21 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
# Keep heap modest for the memory-constrained Jetson.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
