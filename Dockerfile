# Etapa 1: Compilación con Maven y OpenJDK 23
FROM maven:3.9-eclipse-temurin-23 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Etapa 2: Imagen ligera para ejecución con JRE 23
FROM eclipse-temurin:23-jre
WORKDIR /app

COPY --from=build /app/target/backend-*.jar app.jar


ENTRYPOINT ["java", "-jar", "app.jar"]
