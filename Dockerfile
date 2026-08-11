# Build stage
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -DskipTests

COPY src/ src/

RUN ./mvnw clean package -DskipTests


# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

ENV TZ=Asia/Kolkata

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=Asia/Kolkata", "-jar", "app.jar"]