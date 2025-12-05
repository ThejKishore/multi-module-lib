## Reference Dockerfile used for internal tooling/tests
## This version mirrors the production Dockerfile: it expects a host-built JAR
## and constructs a minimal custom JRE using jdeps + jlink.

# -----------------------------
# Stage 1: Obtain dependency information from the host-built JAR
# -----------------------------
FROM eclipse-temurin:21-jdk-jammy AS jre-builder-deps
WORKDIR /work

# Path to the host-built jar relative to the build context
ARG APP_JAR=build/libs/*.jar

# Copy application jar
COPY ${APP_JAR} /work/app.jar

# Compute module dependencies used by the app
RUN jdeps \
      --multi-release 21 \
      --print-module-deps \
      --ignore-missing-deps \
      /work/app.jar > /work/deps.txt

# -----------------------------
# Stage 2: Build the custom JRE with jlink
# -----------------------------
FROM eclipse-temurin:21-jdk-jammy AS jre-builder
WORKDIR /work

COPY --from=jre-builder-deps /work/app.jar /work/app.jar
COPY --from=jre-builder-deps /work/deps.txt /work/deps.txt

# Known extra modules required by Spring Boot/Tomcat on a minimal JRE
RUN echo "jdk.crypto.ec,java.desktop,java.management" > /work/extras.txt \
 && MODS=$(cat /work/deps.txt) \
 && EXTRAS=$(tr -d '\n' < /work/extras.txt) \
 && jlink \
        --add-modules ${MODS},${EXTRAS} \
        --no-header-files \
        --no-man-pages \
        --strip-debug \
        --compress=2 \
        --output /jre

# -----------------------------
# Stage 3: Minimal runtime image (distroless, non-root)
# -----------------------------
FROM gcr.io/distroless/base-debian12:nonroot
ENV TZ=UTC
WORKDIR /app

COPY --from=jre-builder /jre /opt/jre
ENV PATH=/opt/jre/bin:$PATH

ARG APP_JAR=build/libs/*.jar
COPY ${APP_JAR} /app/app.jar

EXPOSE 8080
USER nonroot

ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-jar","/app/app.jar"]