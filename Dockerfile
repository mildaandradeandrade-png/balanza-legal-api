# ===== BUILD =====
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

# Arregla CRLF antes de ejecutar
RUN sed -i 's/\r$//' gradlew
RUN chmod +x gradlew

RUN ./gradlew bootJar --no-daemon

# ===== RUN =====
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
CMD ["sh","-c","java -jar app.jar --server.port=${PORT}"]
