#!/bin/bash
# Item viewer check for one layer: viewer_check.sh <layer> <viewer> <deps> <click x,y> <type> <hover x,y[;x,y]>
# Runs the viewer tour (scripts/client_tour.py --viewer) and copies the screenshots to docs/frames/<layer>_viewer_<viewer>_*.png.
layer=$1; viewer=$2; deps=$3
export ORANGEPEEL_CLICK="$4" ORANGEPEEL_TYPE="$5" ORANGEPEEL_HOVER="$6"
cd "$(dirname "$0")/.."
python scripts/client_tour.py --viewer "$viewer" --deps "$deps" "$layer"
for f in "$layer"/run/screenshots/orangepeel_viewer_*; do
  [ -f "$f" ] && cp "$f" "docs/frames/${layer}_$(basename "$f" | sed 's/^orangepeel_//')"
done
