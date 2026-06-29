FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Djava.io.tmpdir=/app/tmp"

RUN mkdir -p /app/tmp /app/uploads/products

COPY --from=build /workspace/target/ssm-shop-1.0.0.jar /app/ssm-shop.jar
COPY uploads /app/default-uploads
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/app/docker-entrypoint.sh"]
