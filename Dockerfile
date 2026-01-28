FROM gradle:8.4-jdk17 AS builder
WORKDIR /app

# 1. Gradle 캐시 최적화
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY gradlew ./
RUN chmod +x gradlew
RUN ./gradlew dependencies

# 2. 소스 코드 복사 & 빌드
COPY src src
RUN ./gradlew clean bootJar -x test

FROM eclipse-temurin:17-jre
WORKDIR /app

# bootJar만 app.jar로 복사
COPY --from=builder /app/build/libs/*-boot.jar app.jar

# Spring Boot 실행
CMD ["java", "-jar", "app.jar"]
