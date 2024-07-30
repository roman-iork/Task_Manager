FROM gradle:8.7.0-jdk-21-and-22

WORKDIR /app

COPY ./app .

RUN gradle installBootDist

CMD ./build/install/app-boot/bin/app
