# ---- build stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build

# copy only pom first for dependency download cache
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

# copy source and build
COPY src ./src
RUN mvn -B -DskipTests package

# ---- runtime stage ----
FROM eclipse-temurin:17-jdk-jammy

ENV LANG=C.UTF-8 LC_ALL=C.UTF-8

# install libreoffice + fonts (minimal)
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      libreoffice \
      libreoffice-writer \
      fonts-dejavu-core \
      fontconfig \
      libxext6 libxrender1 libxtst6 \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# copy the built uber-jar from builder (assembly plugin produces jar-with-dependencies)
COPY --from=build /build/target/*-jar-with-dependencies.jar /app/app.jar

# copy template if you want it baked in (optional)
COPY src/main/resources/Template.docx /app/Template.docx

# create output dir (so host mount easily maps)
RUN mkdir -p /app/output

# default command
ENTRYPOINT ["java","-jar","/app/app.jar"]
