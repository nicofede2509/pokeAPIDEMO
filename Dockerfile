# Etapa 1: Construcción (Build)
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .

# TRUCO ANTI-WINDOWS: Convertir saltos de línea a formato Linux
RUN sed -i 's/\r$//' gradlew

# Damos permisos y compilamos ignorando los tests
RUN chmod +x gradlew
RUN ./gradlew build -x test

# Etapa 2: Producción (Run)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]