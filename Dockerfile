FROM gradle:jdk17-jammy AS build
COPY --chown=gradle:gradle . /app
WORKDIR /app
RUN chmod +x gradlew
RUN ./gradlew build --no-daemon

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
RUN wget https://dl.google.com/cloudsql/cloud_sql_proxy.linux.amd64 -O cloud_sql_proxy \
    && chmod +x cloud_sql_proxy

COPY --from=build /app/build/libs/*-0.0.1-SNAPSHOT.jar app.jar
COPY ./render-access.json /secrets/render-access.json
RUN chmod 400 /secrets/render-access.json
ENTRYPOINT ["sh", "-c", "./cloud_sql_proxy -instances=folkloric-clock-391008:asia-northeast3:ureca-3-unity=tcp:3306 --credentials-file=/secrets/render-access.json & java -jar app.jar & wait"]