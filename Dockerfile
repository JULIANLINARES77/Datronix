FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

RUN apk add --no-cache ca-certificates wget \
    && update-ca-certificates \
    && wget -q https://cockroachlabs.cloud/clusters/1db28f92-372b-4b79-be59-7ff031d19765/cert -O /app/root.crt \
    && chmod 0444 /app/root.crt \
    && apk del wget

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar /app.jar

ENV SPRING_DATASOURCE_URL="jdbc:postgresql://datronix-35292.j77.aws-us-east-1.cockroachlabs.cloud:26257/defaultdb?sslmode=verify-full&sslrootcert=/app/root.crt"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
