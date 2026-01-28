FROM gradle:jdk17-jammy AS build
COPY --chown=gradle:gradle . /app
WORKDIR /app

RUN chmod +x gradlew
RUN ./gradlew build --no-daemon

FROM eclipse-temurin:17-jdk-jammy
COPY --from=build /app/build/libs/*-0.0.1-SNAPSHOT.jar app.jar
# 컨테이너 시작 시 Cloud SQL Proxy와 Spring Boot 앱 동시 실행
ENTRYPOINT ["/bin/sh", "-c", "\
    /cloud_sql_proxy -dir=/cloudsql -credential_file=/secrets/service-account.json & \
    java -Dspring.datasource.url=jdbc:mysql://127.0.0.1:3306/ureca_compre_project -Dspring.datasource.username=$DB_USERNAME -Dspring.datasource.password=$DB_PASSWORD -jar /app.jar"]