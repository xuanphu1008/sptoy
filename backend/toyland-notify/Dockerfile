# ---- Buoc 1: build file .war bang Maven ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package

# ---- Buoc 2: image chay (chi JRE) ----
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV TZ=Asia/Ho_Chi_Minh
COPY --from=build /src/target/toyland-notify-*.war app.war
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.awt.headless=true", "-jar", "/app/app.war"]
