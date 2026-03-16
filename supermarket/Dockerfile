FROM openjdk:21-ea-jdk-slim
ARG JAR_FILE=target/supermarket-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]