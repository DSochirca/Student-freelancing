FROM gradle:7.3.0-jdk11-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon --stacktrace

EXPOSE 8761

ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions","-jar","build/libs/offersRequestsService-0.0.1-SNAPSHOT.jar"]
