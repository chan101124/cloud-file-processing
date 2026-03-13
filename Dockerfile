FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy pom first to leverage layer caching for dependencies.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN useradd --system --uid 1001 spring

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080
USER spring

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]
