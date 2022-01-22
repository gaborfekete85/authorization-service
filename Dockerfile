FROM openjdk:11
COPY build/libs/authorization-service-latest.jar /app/app.jar
CMD ["java", "-jar", "/app/app.jar"]
EXPOSE 8302