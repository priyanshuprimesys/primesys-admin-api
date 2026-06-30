# --- Stage 1: Build the application ---
FROM maven:3.9.16-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies effectively
COPY pom.xml .
COPY admin-service-common/pom.xml ./admin-service-common/pom.xml
COPY admin-service-mongodb/pom.xml ./admin-service-mongodb/pom.xml
COPY admin-service-server/pom.xml ./admin-service-server/pom.xml

RUN mvn dependency:go-offline -B

# Copy source and compile
COPY . .
RUN mvn clean package -DskipTests

# --- Stage 2: Secure runtime environment ---
# Matches the build stage Java version (17)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create a secure non-root system user
RUN useradd -ms /bin/sh springuser

# Copy the artifact and explicitly assign ownership to springuser
COPY --from=build --chown=springuser:springuser /app/admin-service-server/target/*.jar app.jar

# Switch to the non-root user for security
USER springuser

EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
