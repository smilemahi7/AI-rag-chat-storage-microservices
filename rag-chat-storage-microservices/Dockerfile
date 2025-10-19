# Multi-stage build for production
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:21-jre-alpine

# Install curl and create non-root user
RUN apk add --no-cache curl && \
    addgroup -g 1001 -S ragchat && \
    adduser -u 1001 -S ragchat -G ragchat

WORKDIR /app

# Copy JAR and set ownership
COPY --from=build --chown=ragchat:ragchat /app/target/rag-chat-storage-*.jar app.jar

USER ragchat

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimizations for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]