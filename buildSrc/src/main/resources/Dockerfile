# Stage 1: Build the application JAR using Gradle
FROM gradle:8.12.0-jdk-21-and-23-graal AS builder
LABEL stage="builder"

# Set the working directory inside the container
WORKDIR /home/gradle/project

# Copy only the build configuration to avoid invalidating the cache unnecessarily
COPY build.gradle settings.gradle ./

# Copy the Gradle wrapper files if present
COPY gradlew ./
COPY gradle/ ./gradle/

# Download Gradle dependencies to cache builds efficiently
RUN gradle dependencies --no-daemon

# Copy the rest of the application source code
COPY src ./src

# Build the application JAR
RUN gradle bootJar --no-daemon

# Stage 2: Obtain dependency information for custom JRE creation
FROM amazoncorretto:21-alpine as deps
LABEL stage="deps"

# Copy the application JAR from the builder stage
COPY --from=builder /home/gradle/project/build/libs/*.jar /app/app.jar

# Extract and analyze dependencies
RUN mkdir /app/unpacked && \
    cd /app/unpacked && \
    unzip ../app.jar && \
    cd .. && \
    # Generate list of required modules
    $JAVA_HOME/bin/jdeps \
    --ignore-missing-deps \
    --print-module-deps \
    -q \
    --recursive \
    --multi-release 21 \
    --class-path="./unpacked/BOOT-INF/lib/*" \
    --module-path="./unpacked/BOOT-INF/lib/*" \
    ./app.jar > /deps.info

# Stage 3: Build a custom JRE
FROM amazoncorretto:21-alpine as custom-jre
LABEL stage="custom-jre"

RUN apk add --no-cache binutils

# Copy the dependency information generated in the "deps" stage
COPY --from=deps /deps.info /deps.info

# Create the custom JRE with only necessary modules
RUN $JAVA_HOME/bin/jlink \
    --verbose \
    --add-modules $(cat /deps.info) \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /customjre

# Stage 4: Create the final minimal image
FROM alpine:latest
LABEL stage="final"

# Set the environment variables for Java
ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Copy the custom JRE
COPY --from=custom-jre /customjre $JAVA_HOME

# Create a non-root user for better security
ARG APPLICATION_USER=appuser
RUN adduser --no-create-home -u 1000 -D $APPLICATION_USER

# Create the app directory and set ownership to the non-root user
RUN mkdir /app && chown -R $APPLICATION_USER /app

# Switch to the non-root user
USER 1000

# Copy the application JAR to the image
COPY --from=builder /home/gradle/project/build/libs/*.jar /app/app.jar

# Set the working directory inside the container
WORKDIR /app

# Expose the application port
EXPOSE 8080

# Define the entry point for running the Spring Boot application
ENTRYPOINT [ "/jre/bin/java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar" ]