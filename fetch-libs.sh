#!/usr/bin/env bash
# Populates libs-maven/ with everything the dev runtimes need that is only
# shipped nested inside the Create 0.5.1.i jars (maven.createmod.net hosts
# nothing for MC 1.18/1.19):
#
#  1. flywheel-forge + Registrate from the forge jar's META-INF/jarjar/,
#     installed with minimal hand-written poms under the group `libs.createmod`
#     so Gradle resolves them from the flat repo
#     `maven { url = rootProject.file('libs-maven') }`. The loom forge dev
#     runtime does not unpack jarjar, so they are wired explicitly.
#
#  2. create-fabric's JiJ'd dependencies, extracted RECURSIVELY into
#     libs-maven/fabric-jij/ (porting_lib nests some modules two levels deep:
#     base -> fluids). Fabric Loader does not load nested (META-INF/jars) mods
#     from a dev classpath, so fabric/build.gradle lists them via files().
#
#  3. libs-maven/create-fabric-dev.jar — a copy of create-fabric with its
#     META-INF/jars stripped. Loom's intermediary->named remap of the raw
#     maven artifact silently no-ops while the nested jars are present,
#     leaving intermediary method names in the dev runtime (verified on the
#     mc1.20.1 template with javap: VirtualFluid still had method_15751).
#
# Re-run after bumping the versions in gradle.properties.
set -euo pipefail
cd "$(dirname "$0")"

CREATE_FORGE_URL="https://cdn.modrinth.com/data/LNytGWDc/versions/7BIftSin/create-1.18.2-0.5.1.i.jar"
CREATE_FABRIC_URL="https://api.modrinth.com/maven/maven/modrinth/create-fabric/0.5.1-i-build.1629%2Bmc1.18.2/create-fabric-0.5.1-i-build.1629%2Bmc1.18.2.jar"

mkdir -p libs-maven/fabric-jij
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

install_lib() { # <src> <group-path> <artifact> <version>
  local dir="libs-maven/$2/$3/$4"
  mkdir -p "$dir"
  cp "$1" "$dir/$3-$4.jar"
  cat > "$dir/$3-$4.pom" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>libs.createmod</groupId>
  <artifactId>$3</artifactId>
  <version>$4</version>
  <packaging>jar</packaging>
</project>
EOF
  echo "installed libs.createmod:$3:$4"
}

curl -fsSL --retry 3 "$CREATE_FORGE_URL" -o "$TMP/create-forge.jar"
unzip -j -o "$TMP/create-forge.jar" \
  "META-INF/jarjar/flywheel-forge-1.18.2-0.6.11-107.jar" \
  "META-INF/jarjar/Registrate-MC1.18.2-1.1.3.jar" \
  -d "$TMP" > /dev/null
install_lib "$TMP/flywheel-forge-1.18.2-0.6.11-107.jar" "libs/createmod" "flywheel-forge-1.18.2" "0.6.11-107"
install_lib "$TMP/Registrate-MC1.18.2-1.1.3.jar"       "libs/createmod" "Registrate-MC1.18.2"    "1.1.3"

curl -fsSL --retry 3 "$CREATE_FABRIC_URL" -o "$TMP/create-fabric.jar"

# flywheel-fabric is also needed on the COMMON compile classpath
# (KineticData extends flywheel's BasicData).
unzip -j -o "$TMP/create-fabric.jar" "META-INF/jars/flywheel-fabric-1.18.2-0.6.11-40.jar" -d "$TMP" > /dev/null
install_lib "$TMP/flywheel-fabric-1.18.2-0.6.11-40.jar" "libs/createmod" "flywheel-fabric-1.18.2" "0.6.11-40"

# Stripped local copy for the fabric dev runtime (no `zip` on this machine, so
# the nested jars are removed with python's zipfile module).
python3 - "$TMP/create-fabric.jar" "libs-maven/create-fabric-dev.jar" <<'EOF'
import sys, zipfile
src, dst = sys.argv[1], sys.argv[2]
with zipfile.ZipFile(src) as zin, zipfile.ZipFile(dst, "w", zipfile.ZIP_DEFLATED) as zout:
    for info in zin.infolist():
        if info.filename.startswith("META-INF/jars/"):
            continue
        zout.writestr(info, zin.read(info.filename))
print("wrote", dst, "without META-INF/jars")
EOF

# JiJ'd deps, recursively (porting_lib: base -> fluids nests two levels deep).
unzip -o -j "$TMP/create-fabric.jar" "META-INF/jars/*.jar" -d "libs-maven/fabric-jij" > /dev/null
pass=0
for pass in 1 2 3; do
    found=0
    for jar in libs-maven/fabric-jij/*.jar; do
        if unzip -l "$jar" 2>/dev/null | grep -q "META-INF/jars/.*\.jar"; then
            unzip -o -j "$jar" "META-INF/jars/*.jar" -d "libs-maven/fabric-jij" > /dev/null
            found=1
        fi
    done
    [ "$found" -eq 0 ] && break
done

echo "extracted $(ls libs-maven/fabric-jij | wc -l) JiJ jars into libs-maven/fabric-jij (recursion depth $((pass - 1)))"
