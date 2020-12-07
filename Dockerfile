FROM maven:3.6.3-jdk-11

# Google Chrome

ARG CHROME_VERSION=87.0.4280.88-1
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
        && echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list \
        && apt-get update -qqy \
        && apt-get -qqy install google-chrome-stable=$CHROME_VERSION \
        && rm /etc/apt/sources.list.d/google-chrome.list \
        && rm -rf /var/lib/apt/lists/* /var/cache/apt/* \
        && sed -i 's/"$HERE\/chrome"/"$HERE\/chrome" --no-sandbox/g' /opt/google/chrome/google-chrome

# ChromeDriver

ARG CHROME_DRIVER_VERSION=87.0.4280.88
RUN wget -q -O /tmp/chromedriver.zip https://chromedriver.storage.googleapis.com/$CHROME_DRIVER_VERSION/chromedriver_linux64.zip \
        && unzip /tmp/chromedriver_linux64.zip -d /opt \
        && rm /tmp/chromedriver_linux64.zip \
        && mv /opt/chromedriver /opt/chromedriver-$CHROME_DRIVER_VERSION \
        && chmod 755 /opt/chromedriver-$CHROME_DRIVER_VERSION \
        && ln -s /opt/chromedriver-$CHROME_DRIVER_VERSION /usr/bin/chromedriver

COPY ./ ./home/ws/

RUN chmod -R 777 /home/ws/

RUN mvn -f /home/ws/pom.xml clean package

EXPOSE 4444

CMD ["java", "-jar", "/home/ws/run.jar"]