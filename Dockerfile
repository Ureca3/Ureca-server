FROM gradle:jdk17-jammy AS build
COPY --chown=gradle:gradle . /app
WORKDIR /app
RUN gradle build --no-daemon

FROM eclipse-temurin:17-jdk-jammy
COPY --from=build /app/build/libs/*-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]