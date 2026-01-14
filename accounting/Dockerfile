FROM eclipse-temurin:21-jdk-alpine AS dependencies
LABEL authors="finskiypavel@gmail.com"
WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY saga-core/pom.xml saga-core/
COPY accounting/pom.xml accounting/

COPY profiler/pom.xml profiler/
COPY vacation/pom.xml vacation/
COPY resources/pom.xml resources/
COPY saga/pom.xml saga/

RUN ./mvnw -B -pl accounting -am dependency:go-offline dependency:resolve-plugins

FROM eclipse-temurin:21-jdk AS build
LABEL authors="finskiypavel@gmail.com"
WORKDIR /build
COPY --from=dependencies /root/.m2 /root/.m2

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY saga-core/ saga-core/
COPY accounting/ accounting/

COPY profiler/pom.xml profiler/
COPY vacation/pom.xml vacation/
COPY resources/pom.xml resources/
COPY saga/pom.xml saga/

RUN ./mvnw -B clean package -pl accounting -am -DskipTests

FROM eclipse-temurin:21-jdk-alpine AS extract
WORKDIR /build
COPY --from=build /build/accounting/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:21-jre-alpine
LABEL authors="finskiypavel@gmail.com"

WORKDIR /app

RUN addgroup -S spring && adduser -S -G spring spring
USER spring

COPY --from=extract --chown=spring:spring build/dependencies/ ./
COPY --from=extract --chown=spring:spring build/spring-boot-loader/ ./
COPY --from=extract --chown=spring:spring build/snapshot-dependencies/ ./
COPY --from=extract --chown=spring:spring build/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]