FROM maven
RUN apt -y update && apt -y install gnupg && apt -y install gnupg1 && apt -y install gnupg2
RUN sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list'
RUN wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add -
RUN apt -y update && apt -y install google-chrome-stable
WORKDIR /app
COPY . .
RUN mvn package -DskipTests
CMD ["java", "-jar", "target/steam-alert-0.0.1.jar"]