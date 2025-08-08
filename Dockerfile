# Stage 1: Build with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Install LibreOffice in build stage
RUN apt-get update && \
    apt-get install -y libreoffice && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run with lightweight JDK
FROM openjdk:17-jdk-slim

# Install LibreOffice in runtime stage
RUN apt-get update && \
    apt-get install -y libreoffice && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/target/*-jar-with-dependencies.jar app.jar

CMD ["java", "-jar", "app.jar"]
