FROM openjdk:15

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-Xms128m", "-Xmx512m", "-jar","/app.jar"]