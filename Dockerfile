FROM maven
WORKDIR /app
COPY . .
RUN mvn package -DskipTests
CMD ["java", "-jar", "target/steam-alert-0.0.1.jar"]