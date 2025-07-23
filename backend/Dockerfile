FROM gradle:8.11.1-jdk23 AS build

COPY ./ /app/
WORKDIR /app
RUN gradle --no-daemon clean build -x test

FROM openjdk:23-jdk
ARG version
# todo temp solution until I figure what to do about versioning
COPY --from=build /app/build/libs/reline-*.jar /usr/local/lib/app.jar
ENTRYPOINT ["java","-jar","/usr/local/lib/app.jar"]