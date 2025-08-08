FROM openjdk:17-jdk-slim

# Install LibreOffice
RUN apt-get update && \
    apt-get install -y libreoffice && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy Maven build files and source
WORKDIR /app
COPY . .

# Build the project
RUN ./mvnw package -DskipTests

# Run the jar
CMD ["java", "-jar", "target/*-jar-with-dependencies.jar"]
