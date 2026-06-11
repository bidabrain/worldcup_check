#!/bin/sh
# Wrapper that uses the already-cached Gradle 8.2 distribution
GRADLE_HOME="$HOME/.gradle/wrapper/dists/gradle-8.2-bin/bbg7u40eoinfdyxsxr3z4i7ta/gradle-8.2"
exec "$GRADLE_HOME/bin/gradle" "$@"
