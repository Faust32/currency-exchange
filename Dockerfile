FROM maven:3.9.6-eclipse-temurin-21 AS BUILD

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean compile package

FROM tomcat:10.1.30-jdk22

WORKDIR /usr/local/tomcat/webapps

COPY --from=build /app/target/CurrencyExchange-1.0-SNAPSHOT.war ./CurrencyExchange-1.0-SNAPSHOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]

