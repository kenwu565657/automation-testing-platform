FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache chromium chromium-chromedriver

WORKDIR /app

COPY build/libs/engine-service-*-fat.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]