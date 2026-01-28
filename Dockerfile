FROM gradle:jdk17-jammy AS build
COPY --chown=gradle:gradle . /app
WORKDIR /app
RUN chmod +x gradlew
RUN ./gradlew build --no-daemon -x test # 테스트 제외로 빌드 속도 향상

# ... (빌드 단계 생략: gradle:jdk17-jammy AS build 등 기존 코드 유지)

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Cloud SQL Auth Proxy 설치 및 권한 설정
ARG CLOUD_SQL_PROXY_SHA256="8c6d76380f4b7005473eb2e13991d6239f90da021cf58d91d062739479e577cf"
RUN wget -q https://storage.googleapis.com/cloud-sql-connectors/cloud-sql-proxy/v2.8.1/cloud-sql-proxy.linux.amd64 -O /usr/local/bin/cloud_sql_proxy && \
     echo "${CLOUD_SQL_PROXY_SHA256}  /usr/local/bin/cloud_sql_proxy" | sha256sum -c - && \
     chmod +x /usr/local/bin/cloud_sql_proxy

COPY --from=build /app/build/libs/*.jar app.jar
COPY ./render-access.json /secrets/render-access.json
RUN chmod 400 /secrets/render-access.json

ENTRYPOINT ["sh", "-c", "./cloud_sql_proxy --address 127.0.0.1 --port 3306 --credentials-file /secrets/render-access.json folkloric-clock-391008:asia-northeast3:ureca-3-unity & sleep 30; java -jar app.jar"]