# ── Build stage ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Cache dependencies separately from source so code changes don't bust the
# dependency download layer on every rebuild.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ── Run stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Run as a non-root user — defense in depth in case the container is ever compromised.
RUN useradd --no-create-home --shell /usr/sbin/nologin appuser
COPY --from=build /app/target/*.jar app.jar
RUN chown appuser:appuser app.jar
USER appuser

# Use the prod profile by default; override at runtime via SPRING_PROFILES_ACTIVE env var.
ENV SPRING_PROFILES_ACTIVE=prod

# PORT is injected by the hosting platform (Render, Railway, Fly.io, etc.).
# Fallback to 8080 for local Docker runs.
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
