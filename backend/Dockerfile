FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Leverage Docker layer caching for dependencies.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src/ src/
RUN ./mvnw -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar

# Render sets PORT=10000 by default for web services; Spring reads it via server.port=${PORT:8080}.
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]

