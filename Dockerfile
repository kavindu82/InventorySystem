# Step 1: Use official Maven image to build the app
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Use JDK image to run the app
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/Inventory-0.0.1-SNAPSHOT.jar app.jar

# Step 3: Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
