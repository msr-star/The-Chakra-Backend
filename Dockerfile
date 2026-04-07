# Build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# 1. Copy only pom.xml to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Run stage
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
