FROM maven:3.9.6-eclipse-temurin-21 AS BUILD
WORKDIR /app
COPY pom.xml .

RUN mvn dependency:go-offline -B
COPY src ./src

RUN mvn clean package -DskipTests

FROM tomcat:10.1.28-jdk21
WORKDIR /usr/local/tomcat/webapps
COPY --from=build /app/target/*.war ./api.war
EXPOSE 8080
CMD ["catalina.sh", "run"]

