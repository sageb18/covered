# The build context is the REPO ROOT, not covered/, because this image needs both
# the Spring Boot backend and the React frontend. That is why this file sits here
# and not next to the pom. On Render, leave "Root Directory" blank.

# ---------------------------------------------------------------------------
# Stage 1 - compile the React app into static files
# ---------------------------------------------------------------------------
FROM node:22-slim AS frontend

WORKDIR /frontend

# Copy the manifest and lockfile on their own layer first. Docker caches each
# layer, so `npm ci` is only re-run when your dependencies actually change -
# not every time you edit a .jsx file.
COPY covered-frontend/package.json covered-frontend/package-lock.json ./
RUN npm ci

COPY covered-frontend/ ./
RUN npm run build

# ---------------------------------------------------------------------------
# Stage 2 - build the Spring Boot jar with the React build baked into it
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS backend

WORKDIR /build

# Same caching trick: resolve dependencies from the pom alone, so the large
# Timefold/Spring download is skipped on builds where only Java source changed.
COPY covered/pom.xml ./
RUN mvn -B dependency:go-offline

COPY covered/src/ ./src/

# Spring Boot serves anything on the classpath under /static at the web root.
# Dropping Vite's output there is the whole trick behind one jar serving both
# the API and the UI from a single origin - which is what makes the relative
# /api/... paths in your api.js work in production with no CORS setup.
COPY --from=frontend /frontend/dist/ ./src/main/resources/static/

RUN mvn -B clean package -DskipTests

# ---------------------------------------------------------------------------
# Stage 3 - the runtime image. Only the jar and a JRE ship; the JDK, Maven,
# Node and node_modules from the stages above are all discarded.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=backend /build/target/covered-*.jar app.jar

# Render's free tier caps the service at 512MB. The JVM's default max heap is
# 1/4 of available memory, which leaves the Timefold solver very little room to
# work in. This image runs one process, so it can safely claim more.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# Documentation for readers plus a hint to Render's port detection. The actual
# bind port comes from the PORT env var - see application.properties.
EXPOSE 8080

# Don't run as root.
RUN useradd --system --uid 1001 spring
USER spring

# `sh -c` so $JAVA_OPTS gets expanded, and `exec` so the JVM becomes PID 1 and
# receives Render's shutdown signal directly instead of the shell absorbing it.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
