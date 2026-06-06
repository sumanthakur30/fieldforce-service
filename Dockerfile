FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY security-common ./security-common
RUN mvn -f security-common/pom.xml -B -DskipTests install

COPY fieldforce-service ./fieldforce-service
RUN mvn -f fieldforce-service/pom.xml -B -DskipTests package && cp /workspace/fieldforce-service/target/*-SNAPSHOT.jar /workspace/fieldforce-service/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/fieldforce-service/app.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
