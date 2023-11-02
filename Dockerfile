FROM maven:3.9-eclipse-temurin-8 as build
WORKDIR /staging

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN unset MAVEN_CONFIG && ./mvnw install -q -B -D skipTests

FROM maven:3.9-eclipse-temurin-8
WORKDIR /app

COPY --from=build /staging/target/*.jar /app/mia.jar
COPY --from=build /staging/src/main/resources /app/src/main/resources

ENTRYPOINT ["java", "-jar", "./mia.jar"]