FROM adoptopenjdk/openjdk11:jdk-11.0.9_11.1-alpine-slim AS MAVEN_BUILD

ARG MAVEN_VERSION=3.6.3
ARG USER_HOME_DIR="/root"
ARG SHA=c35a1803a6e70a126e80b2b3ae33eed961f83ed74d18fcd16909b2d44d7dada3203f1ffe726c17ef8dcca2dcaa9fca676987befeadc9b9f759967a8cb77181c0
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

RUN apk update && apk add --no-cache git \
        curl

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
        && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
        && echo "${SHA}  /tmp/apache-maven.tar.gz" | sha512sum -c - \
        && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
        && rm -f /tmp/apache-maven.tar.gz \
        && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

CMD ["/usr/bin/mvn"]

COPY ./ ./home/ws/

RUN chmod -R 777 /home/ws/

RUN mvn -f /home/ws/pom.xml clean package

FROM adoptopenjdk/openjdk11:jdk-11.0.9_11-alpine-slim

ENV PATH="/usr/bin/chromedriver:${PATH}"

COPY --from=MAVEN_BUILD /home/ws/config/linkscraper-settings.xml /home/ws/config/linkscraper-settings.xml
COPY --from=MAVEN_BUILD /home/ws/target/ws-tradera-ls.jar /home/ws/run.jar

RUN apk update && apk add --no-cache wget \
        alsa-lib \
        at-spi2-atk \
        atk \
        cairo \
        cups-libs \
        dbus-libs \
        eudev-libs \
        expat \
        flac \
        gdk-pixbuf \
        glib \
        libgcc \
        libjpeg-turbo \
        libpng \
        libwebp \
        libx11 \
        libxcomposite \
        libxdamage \
        libxext \
        libxfixes \
        tzdata \
        libexif \
        udev \
        xvfb \
        zlib-dev \
        chromium \
        chromium-chromedriver

EXPOSE 4444

CMD ["java", "-jar", "/home/ws/run.jar"]
