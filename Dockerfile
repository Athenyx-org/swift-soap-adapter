# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy only files needed to resolve dependencies first (better caching)
COPY gradle gradle
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .

# Ensure wrapper is executable (important on Linux/git clones)
RUN chmod +x gradlew

# Warm up dependency cache (faster incremental builds)
RUN ./gradlew --no-daemon --stacktrace build -x test || true

# Now copy the full source and do the real build
COPY . .
RUN ./gradlew --no-daemon --stacktrace clean build -x test

# Run stage
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]