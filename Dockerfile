# ---- Build stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# cache deps
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# build
COPY . .
RUN mvn -q -DskipTests package

# ---- Run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# copy the built jar (adjust if your jar name is fixed)
COPY --from=build /app/target/*.jar app.jar

# Railway expects the app to listen on $PORT
ENV PORT=8080
EXPOSE 8080

# keep JVM memory within container limits
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

CMD ["java","-jar","app.jar"]
