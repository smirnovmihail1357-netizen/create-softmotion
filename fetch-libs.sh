#!/usr/bin/env bash
# Extract create-fabric's JiJ'd dependencies into libs-maven/fabric-jij/.
# Fabric Loader does not load nested (META-INF/jars) mods from a dev classpath,
# so the fabric dev runtime lists them explicitly via files() dependencies.
# porting_lib nests some modules two levels deep (base -> fluids), so the
# extraction is recursive until no more nested jars appear.
set -euo pipefail
cd "$(dirname "$0")"

URL="https://cdn.modrinth.com/data/Xbc0uyRg/versions/HAqwA6X1/create-fabric-6.0.8.1%2Bbuild.1744-mc1.20.1.jar"
DEST="libs-maven/fabric-jij"
TMP="$(mktemp -d)"

mkdir -p "$DEST"
curl -fL --retry 3 -o "$TMP/create-fabric.jar" "$URL"

# A loom remap of create-fabric silently no-ops while the jar still contains its
# nested META-INF/jars (all of which are also added explicitly below), leaving
# intermediary method names in the dev runtime. Strip them from the local copy
# used as a files() dependency.
cp "$TMP/create-fabric.jar" "libs-maven/create-fabric-dev.jar"
zip -q -d "libs-maven/create-fabric-dev.jar" 'META-INF/jars/*' 'META-INF/jars'

unzip -o -j "$TMP/create-fabric.jar" 'META-INF/jars/*.jar' -d "$DEST" > /dev/null

# Recursively extract jars nested inside the extracted jars.
for pass in 1 2 3; do
    found=0
    for jar in "$DEST"/*.jar; do
        if unzip -l "$jar" 2>/dev/null | grep -q "META-INF/jars/.*\.jar"; then
            unzip -o -j "$jar" 'META-INF/jars/*.jar' -d "$DEST" > /dev/null
            found=1
        fi
    done
    [ "$found" -eq 0 ] && break
done
rm -rf "$TMP"

echo "extracted $(ls "$DEST" | wc -l) JiJ jars into $DEST (recursion depth $((pass - 1)))"
