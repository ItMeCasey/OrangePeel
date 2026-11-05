#!/bin/bash
# Runs the item viewer check on every layer and viewer that has an upstream build (see docs/COMPATIBILITY.md).
# Search box and first-result coordinates are screenshot pixels on the 1904x1041 game window; they differ per viewer.
cd "$(dirname "$0")/.."
C=scripts/viewer_check.sh
JEI_XY=("1610,992" "1428,92"); REI_XY=("890,1020" "1272,102"); BEI_XY=("1540,925" "1300,98"); EMI_XY=("950,1004" "1278,87")
run() { echo "== $1 $2"; $C "$1" "$2" "$3" "${4%%|*}" "golden apple" "${4##*|}" 2>&1 | grep -E "TOUR"; }
REI="curse.maven:roughly-enough-items-310111"
run neoforge-1.21.1 rei "$REI:6199140,curse.maven:architectury-api-419699:8492726,curse.maven:cloth-config-348521:5729127" "890,1020|1272,102"
run neoforge-26.1.2 jei curse.maven:jei-238222:9384706 "1610,992|1428,92"
run neoforge-26.1.2 rei "$REI:8271478,curse.maven:architectury-api-419699:9111154,curse.maven:cloth-config-348521:7817583" "890,1020|1272,102"
run neoforge-26.1.2 bei curse.maven:bei-1783910:9438907 "1540,925|1300,98"
run neoforge-26.2 jei curse.maven:jei-238222:9352384 "1610,992|1428,92"
run neoforge-26.2 rei "$REI:8271756,curse.maven:architectury-api-419699:9111233,curse.maven:cloth-config-348521:8269700" "890,1020|1272,102"
run neoforge-26.2 bei curse.maven:bei-1783910:9451209 "1540,925|1300,98"
run neoforge-26.3 jei curse.maven:jei-238222:9453343 "1610,992|1428,92"
run neoforge-26.3 rei "$REI:9090699,curse.maven:architectury-api-419699:9111315,curse.maven:cloth-config-348521:8269700" "890,1020|1272,102"
run neoforge-26.3 bei curse.maven:bei-1783910:9451197 "1540,925|1300,98"
run forge-1.21.1 jei curse.maven:jei-238222:9393325 "1610,992|1428,92"
run forge-1.21.1 bei curse.maven:bei-1783910:9438915 "1540,925|1300,98"
run forge-26.1.2 bei curse.maven:bei-1783910:9438905 "1540,925|1300,98"
run forge-26.2 bei curse.maven:bei-1783910:9451199 "1540,925|1300,98"
run forge-26.3 bei curse.maven:bei-1783910:9451187 "1540,925|1300,98"
echo MATRIX DONE
