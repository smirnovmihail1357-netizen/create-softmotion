#!/usr/bin/env bash
# Populate libs-maven/ (local file-maven mirror) with Create dev dependencies.
# maven.createmod.net currently has an expired TLS cert, so we take the jars
# from Modrinth CDN (the same published release files) instead.
set -euo pipefail
cd "$(dirname "$0")"

CREATE_URL="https://cdn.modrinth.com/data/LNytGWDc/versions/UjX6dr61/create-1.21.1-6.0.10.jar" # Create 6.0.10, mc1.21.1, NeoForge
CREATE_DIR="libs-maven/com/simibubi/create/create-1.21.1/6.0.10"
FW_DIR="libs-maven/dev/engine-room/flywheel/flywheel-neoforge-1.21.1/1.0.6"

mkdir -p "$CREATE_DIR" "$FW_DIR"
curl -fL --retry 3 -o "$CREATE_DIR/create-1.21.1-6.0.10.jar" "$CREATE_URL"

# Flywheel 1.0.6 is shipped inside Create's jar (META-INF/jarjar) - extract it
# for the compileOnly classpath (RotatingInstance extends flywheel lib classes).
unzip -p "$CREATE_DIR/create-1.21.1-6.0.10.jar" META-INF/jarjar/flywheel-neoforge-1.21.1-1.0.6.jar \
    > "$FW_DIR/flywheel-neoforge-1.21.1-1.0.6.jar"

pom() { # groupId artifactId version -> pom to stdout
    printf '<?xml version="1.0" encoding="UTF-8"?>\n<project xmlns="http://maven.apache.org/POM/4.0.0">\n  <modelVersion>4.0.0</modelVersion>\n  <groupId>%s</groupId>\n  <artifactId>%s</artifactId>\n  <version>%s</version>\n  <packaging>jar</packaging>\n</project>\n' "$1" "$2" "$3"
}
pom com.simibubi create-1.21.1 6.0.10 > "$CREATE_DIR/create-1.21.1-6.0.10.pom"
pom dev.engine-room.flywheel flywheel-neoforge-1.21.1 1.0.6 > "$FW_DIR/flywheel-neoforge-1.21.1-1.0.6.pom"
echo "libs-maven ready."
