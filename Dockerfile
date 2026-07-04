# syntax=docker/dockerfile:1.7

# Stage 1 - Build Application (deps + package combined)

FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Warms the Maven cache. Reused automatically on every future build.
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -B

COPY src src

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean package -DskipTests -B

# Stage 2 - Runtime
FROM eclipse-temurin:21-jre-jammy
LABEL maintainer="your-name"
LABEL description="Spring Boot Production Image"

WORKDIR /app

# Create non-root user with a proper home dir (avoids JVM tmp-dir warnings)
RUN groupadd -r spring && \
    useradd -r -g spring -d /home/spring -m spring

COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

USER spring

ENV JAVA_OPTS="-XX:+UseContainerSupport \
-XX:MaxRAMPercentage=75.0 \
-XX:+ExitOnOutOfMemoryError"
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8080

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]