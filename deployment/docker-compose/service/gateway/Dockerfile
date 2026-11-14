FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY platform-gateway/build/libs/gateway-service-*.jar app.jar
EXPOSE 18080
ENTRYPOINT ["java", "-jar", "app.jar"]