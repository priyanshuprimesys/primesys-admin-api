FROM bellsoft/liberica-openjdk-alpine:17.0.6
EXPOSE 8080
COPY target/user-service-server-0.0.1-SNAPSHOT.jar  user-service-server-0.0.1.jar
ENTRYPOINT ["sh", "-c", "java -jar /user-service-server-0.0.1.jar"]