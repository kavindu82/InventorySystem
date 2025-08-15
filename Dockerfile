# ---- Build stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline
COPY . .
RUN mvn -q -DskipTests package

# ---- Run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
# copy the fat jar (adjust the name if different)
COPY --from=build /app/target/*.jar app.jar

# Railway sets PORT; Spring must listen on it
ENV PORT=8080
EXPOSE 8080

# optional: improve startup for containers
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

CMD ["java", "-jar", "app.jar"]
