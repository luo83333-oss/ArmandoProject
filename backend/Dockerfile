# NL2-158 production API image
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/market-api-*.jar /app/app.jar
RUN mkdir -p /app/uploads
VOLUME ["/app/uploads"]
EXPOSE 8082
ENV SPRING_PROFILES_ACTIVE=prod
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -fsS http://localhost:8082/api/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
