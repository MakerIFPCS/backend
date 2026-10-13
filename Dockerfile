FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=build /build/target/website-0.0.1-SNAPSHOT.jar app.jar
USER 10001
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
