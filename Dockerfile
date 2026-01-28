# Dockerfile
FROM  openjdk:17-alpine

WORKDIR /app
COPY . /app

RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

CMD ["java", "-jar", "unity-0.0.1-SNAPSHOT.jar"]