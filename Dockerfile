FROM adoptopenjdk/openjdk11:jdk-11.0.9_11.1-alpine-slim
COPY /bin/drivers/chromedriver_linux /bin/drivers/chromedriver_linux
COPY /target/ws-tradera-ls.jar /linkscraper.jar
CMD ["java", "-jar", "/linkscraper.jar"]