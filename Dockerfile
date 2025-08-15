# Use a JDK image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy your built JAR file into the container
COPY target/*.jar app.jar

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
