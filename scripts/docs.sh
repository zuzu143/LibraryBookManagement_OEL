#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p docs/javadoc
javadoc -public -quiet -Xdoclint:all -encoding UTF-8 -charset UTF-8 -d docs/javadoc -sourcepath src -subpackages edu.oel.library
printf 'Generated docs/javadoc/index.html\n'
