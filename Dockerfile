FROM gradle:8.7.0-jdk-21-and-22

WORKDIR .

COPY . .

RUN gradle installBootDist

#CMD ./build/install/app-boot/bin/app

CMD java -jar build/install/app-boot/lib/app-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --sentry.dsn=${SENTRY_AUTH_TOKEN}
