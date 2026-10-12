#!/bin/sh
# xanderView launcher (Linux, macOS). Needs Java 26+ (JAVA_HOME or java on PATH). Natives are loaded from libs/ next to
# the jar (docs/NATIVES.md); the first start copies them into the per-user cache. On macOS the app relaunches itself
# with -XstartOnFirstThread when needed; under Wayland it relaunches with GDK_BACKEND=x11 (PlatformLaunch).
DIR="$(cd "$(dirname "$0")" && pwd)"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"
exec "$JAVA" -Xms256m -Xmx4000m -XX:+UseStringDeduplication -jar "$DIR/xanderview.jar" "$@"
