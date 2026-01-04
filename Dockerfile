#
# Set a variable that can be used in all stages.
#
ARG BUILD_HOME=/build

#
# Gradle image for the build stage.
#
FROM eclipse-temurin:21-jdk-alpine AS build-image

#
# Set the working directory.
#
ARG BUILD_HOME
ENV APP_HOME=$BUILD_HOME
WORKDIR $APP_HOME

#
# Copy only build files first to cache dependencies
#
COPY gradle $APP_HOME/gradle/
COPY gradlew $APP_HOME/
RUN ./gradlew --no-daemon --version

#
# Copy the Gradle configs and source code
# into the build container.
#
COPY build.gradle settings.gradle $APP_HOME/
RUN ./gradlew --no-daemon :dependencies

#
# Build the application.
#
COPY src/main/ $APP_HOME/src/main/
RUN ./gradlew --no-daemon build -x check

#
# Java image for the application to run in.
#
FROM gcr.io/distroless/java21-debian13:nonroot

#
# Copy the jar file in and name it app.jar.
#
ARG BUILD_HOME
ENV APP_HOME=$BUILD_HOME
COPY --from=build-image $APP_HOME/build/libs/rssaggregator-0.0.1-SNAPSHOT.jar app.jar

#
# The command to run when the container starts.
#
CMD ["app.jar"]
