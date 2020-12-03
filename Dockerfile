FROM adoptopenjdk/openjdk11:jdk-11.0.9_11-alpine-slim

ENV PATH="/usr/bin/chromedriver:${PATH}"

COPY /config/linkscraper-settings.xml /home/ws/config/linkscraper-settings.xml
COPY /target/ws-tradera-ls.jar /home/ws/run.jar

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
