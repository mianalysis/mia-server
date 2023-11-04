FROM maven:3.9-eclipse-temurin-8 as build
WORKDIR /staging

# Only copy the pom.xml file to download dependencies
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN unset MAVEN_CONFIG && ./mvnw dependency:go-offline -q -B

# Copy the source code and build the application
COPY src src
RUN unset MAVEN_CONFIG && ./mvnw install -q -B -Dmaven.test.skip -Dmaven.javadoc.skip -Denforcer.skip

FROM maven:3.9-eclipse-temurin-8
WORKDIR /app

COPY --from=build /staging/target/*.jar /app/mia.jar
COPY --from=build /staging/src/main/resources /app/src/main/resources

ENTRYPOINT ["java", "-jar", "./mia.jar"]