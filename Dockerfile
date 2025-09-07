# Dockerfile
FROM amd64/eclipse-temurin:21.0.4_7-jdk-alpine
WORKDIR /app
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
