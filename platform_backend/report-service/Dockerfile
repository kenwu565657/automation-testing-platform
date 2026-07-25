FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/report-service-all.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8082

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]