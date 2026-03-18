#!/bin/sh
# Gradle wrapper script - downloads Gradle 8.2 if needed

APP_BASE_NAME=$(basename "$0")
APP_HOME=$(cd "$(dirname "$0")" && pwd)

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
GRADLE_OPTS="${GRADLE_OPTS} -Dorg.gradle.appname=$APP_BASE_NAME"

# Check for wrapper jar
if [ ! -f "$CLASSPATH" ]; then
    echo "Downloading Gradle wrapper jar..."
    mkdir -p "$APP_HOME/gradle/wrapper"
    curl -sL -o "$CLASSPATH" "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar"
fi

exec java $GRADLE_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
