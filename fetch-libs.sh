#!/usr/bin/env bash
# Populate libs/ with the Create jar this branch compiles against.
set -euo pipefail
cd "$(dirname "$0")"

# Create 0.3.2g for mc1.16.5 (Forge) - the only Create that exists for 1.16.5.
# CurseForge project "create", file id 3536025, served from the CurseForge CDN.
CREATE_URL="https://mediafilez.forgecdn.net/files/3536/25/create-mc1.16.5_v0.3.2g.jar"
# Flywheel 1.16-0.2.5, Create 0.3.2g's mandatory runtime dependency.
# CurseForge project "flywheel" (Flywheel Legacy), file id 3535459.
FLYWHEEL_URL="https://mediafilez.forgecdn.net/files/3535/459/flywheel-1.16-0.2.5.jar"

mkdir -p libs
curl -fL --retry 3 -o libs/create-mc1.16.5_v0.3.2g.jar "$CREATE_URL"
curl -fL --retry 3 -o libs/flywheel-1.16-0.2.5.jar "$FLYWHEEL_URL"
echo "libs ready."
