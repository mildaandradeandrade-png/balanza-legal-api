# ===== BUILD =====
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN chmod +x gradlew && ./gradlew bootJar --no-daemon
RUN sed -i 's/\r$//' gradlew
RUN chmod +x gradlew
RUN ./gradlew build --no-daemon

# ===== RUN =====
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

CMD ["sh","-c","java -jar app.jar --server.port=${PORT}"]
