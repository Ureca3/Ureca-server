# 1️⃣ Build stage
FROM gradle:8.4-jdk17 AS builder
WORKDIR /app

# 캐시 최적화
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY gradlew ./
RUN chmod +x gradlew
RUN ./gradlew dependencies

# 소스 복사 & 빌드
COPY src src
RUN ./gradlew clean bootJar -x test

# 2️⃣ Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
