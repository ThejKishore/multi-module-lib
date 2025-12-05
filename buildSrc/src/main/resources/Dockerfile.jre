# Use the official Eclipse Temurin JRE 21 image based on Ubuntu Jammy
FROM eclipse-temurin:21-jre-jammy

# Set the working directory inside the container
WORKDIR /app

ARG APP_JAR=build/libs/*boot.jar

# Copy the executable Spring Boot jar from the build context
COPY ${APP_JAR} /app/app.jar


# Command to run the application when the container starts
CMD ["java", "-jar", "app.jar"]