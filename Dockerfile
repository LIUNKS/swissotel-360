# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /workspace

ARG MODULE_PATH

COPY mvnw pom.xml ./
COPY .mvn ./.mvn
COPY libs/shared-kernel/pom.xml ./libs/shared-kernel/pom.xml
COPY libs/shared-security/pom.xml ./libs/shared-security/pom.xml
COPY gateway/pom.xml ./gateway/pom.xml
COPY services/iam-service/pom.xml ./services/iam-service/pom.xml
COPY services/guests-service/pom.xml ./services/guests-service/pom.xml
COPY services/rooms-service/pom.xml ./services/rooms-service/pom.xml
COPY services/reservations-service/pom.xml ./services/reservations-service/pom.xml
COPY services/operations-service/pom.xml ./services/operations-service/pom.xml
COPY services/billing-service/pom.xml ./services/billing-service/pom.xml

RUN sed -i 's/\r$//' mvnw .mvn/wrapper/maven-wrapper.properties && chmod +x mvnw

COPY libs ./libs
COPY gateway ./gateway
COPY services ./services

RUN --mount=type=cache,id=swissotel-m2,target=/root/.m2 \
    test -n "${MODULE_PATH}" \
    && ./mvnw -B -pl "${MODULE_PATH}" -am package -DskipTests \
    && JAR_PATH="$(find "${MODULE_PATH}/target" -maxdepth 1 -type f -name '*.jar' ! -name '*.original' ! -name '*-plain.jar' | head -n 1)" \
    && test -n "${JAR_PATH}" \
    && mkdir -p /app \
    && cp "${JAR_PATH}" /app/app.jar

FROM eclipse-temurin:25-jre-alpine AS runtime
ARG APP_PORT=8080

RUN apk add --no-cache tzdata \
    && addgroup -S swissotel \
    && adduser -S -G swissotel swissotel

ENV APP_PORT=${APP_PORT}
ENV TZ=America/Lima
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Duser.timezone=America/Lima"

WORKDIR /app
COPY --from=builder --chown=swissotel:swissotel /app/app.jar /app/app.jar
USER swissotel

EXPOSE ${APP_PORT}

HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
    CMD wget -qO- "http://127.0.0.1:${APP_PORT}/actuator/health" | grep -Eq '"status"[[:space:]]*:[[:space:]]*"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
