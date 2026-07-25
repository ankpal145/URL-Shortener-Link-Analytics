# syntax=docker/dockerfile:1.7

# ---- UI stage: Vite production build ----
FROM node:22-alpine AS ui
WORKDIR /ui
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

# ---- build stage: compile the fat jar with Maven + JDK 21 ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
COPY --from=ui /ui/dist ./src/main/resources/static
RUN mvn -q -DskipTests package \
    && cp target/url-shortener-analytics-*.jar target/app.jar

# ---- runtime stage: slim JRE image, non-root user ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN useradd --system --uid 1001 --home /app --shell /usr/sbin/nologin appuser \
    && chown -R appuser:appuser /app
USER appuser

COPY --from=build --chown=appuser:appuser /src/target/app.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
