FROM eclipse-temurin:21.0.2_13-jre-jammy
WORKDIR /app
COPY build/libs/dispatch-service.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
