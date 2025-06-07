FROM openjdk:17-jdk-slim
VOLUME /tmp
RUN mkdir -p /app/uploads
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 9001
ENTRYPOINT ["java", "-jar", "/app.jar"]