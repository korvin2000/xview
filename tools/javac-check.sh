#!/usr/bin/env bash
# Compile all three modules with plain javac (no Maven needed), using jars from the local
# Maven repo. A fast "does it still compile?" check of main sources only: no resources, no tests,
# no shading, no runtime/native check (use `mvn package` + jar inspection for that).
# Covers the Windows SWT flavor (vendored thirddep-j7/src/swt-win32); the stock-SWT flavors need Maven
# (they unpack the stock SWT jar, see docs/BUILD.md).
# Output goes to a disposable directory, never to target/.
# Usage: bash tools/javac-check.sh [outdir]   (outdir must be new/empty or a previous javac-check dir)
set -uo pipefail
cd "$(dirname "$0")/.."

winpath() { if command -v cygpath >/dev/null; then cygpath -m "$1"; else echo "$1"; fi; }
ROOT=$(winpath "$(pwd)")
SEP=':'; [[ "$(uname -s)" == MINGW* || "$(uname -s)" == MSYS* || "$(uname -s)" == CYGWIN* ]] && SEP=';'

# --- JDK 21+ -------------------------------------------------------------------------
JAVAC=""
for c in "${JAVA_HOME:-}/bin/javac" "$HOME"/.jdks/temurin-21*/bin/javac "$HOME"/.jdks/*21*/bin/javac "$(command -v javac 2>/dev/null)"; do
  [[ -n "$c" && -x "$c" ]] && { JAVAC="$c"; break; }
done
[[ -z "$JAVAC" ]] && { echo "javac (JDK 21) not found; set JAVA_HOME" >&2; exit 2; }
"$JAVAC" -version 2>&1 | grep -qE 'javac (2[1-9]|[3-9][0-9])' || { echo "need javac 21+: $("$JAVAC" -version 2>&1)" >&2; exit 2; }

M2=$(winpath "${M2_REPO:-$HOME/.m2/repository}")
OUT=$(winpath "${1:-${TMPDIR:-/tmp}/xview-javac}")
MARKER=".xview-javac"
# only ever delete a directory this script created (marker file) or an empty one
if [[ -e "$OUT" ]]; then
  if [[ -f "$OUT/$MARKER" ]]; then rm -rf "$OUT"
  elif [[ -d "$OUT" && -z "$(ls -A "$OUT")" ]]; then :
  else echo "refusing to clear $OUT (not empty and not a javac-check dir)" >&2; exit 2; fi
fi
mkdir -p "$OUT"/{commons,third,app,work} && : > "$OUT/$MARKER"

jar() { # first existing jar among candidates; empty if none
  for p in "$@"; do [[ -f "$p" ]] && { echo "$p"; return; }; done
}
cp_join() { local IFS="$SEP"; echo "$*"; }
# need() runs inside $(...) subshells, so it records misses in a file instead of a variable
need() { local j; j=$(jar "$@"); [[ -z "$j" ]] && { echo "MISSING jar: $1" >&2; echo "$1" >> "$OUT/work/missing.txt"; }; echo "$j"; }

CP_COMMONS=$(cp_join \
  "$(need "$M2/org/slf4j/slf4j-api/1.7.10/slf4j-api-1.7.10.jar")")

CP_THIRD=$(cp_join "$CP_COMMONS" "$OUT/commons" \
  "$(need "$M2/com/jhlabs/filters/2.0.235-1/filters-2.0.235-1.jar")" \
  "$(need "$M2/com/github/umjammer/vavi-commons/vavi-commons/1.1.14/vavi-commons-1.1.14.jar")" \
  "$(need "$M2/net/java/dev/jna/jna/5.15.0/jna-5.15.0.jar")")

CP_APP=$(cp_join "$CP_THIRD" "$OUT/third" \
  "$(need "$M2/com/carrotsearch/hppc/0.8.2/hppc-0.8.2.jar")" \
  "$(need "$M2/edu/wpi/first/thirdparty/frc2023/opencv/opencv-java/4.6.0-5/opencv-java-4.6.0-5.jar")" \
  "$(need "$M2/net/jpountz/lz4/lz4/1.3.0/lz4-1.3.0.jar")" \
  "$(need "$M2/me/xuender/unidecode/0.0.7/unidecode-0.0.7.jar")")
[[ -s "$OUT/work/missing.txt" ]] && { echo "missing jars; run a Maven build once to populate ~/.m2" >&2; exit 2; }

# quoted argfile entries, so paths with spaces work
srcs() { find "$1" -name '*.java' | while read -r f; do printf '"%s"\n' "$(winpath "$f")"; done; }

rc=0
run() { # name release classpath outdir listfile [extra javac args]
  local name=$1 lvl=$2 cp=$3 out=$4 lst=$5; shift 5
  if "$JAVAC" -nowarn -encoding UTF-8 --release "$lvl" "$@" -cp "$cp" -d "$out" @"$lst" > "$OUT/work/$name.log" 2>&1; then
    echo "OK    $name ($(find "$out" -name '*.class' | wc -l) classes)"
  else
    rc=1; echo "FAIL  $name: $(grep -c 'error:' "$OUT/work/$name.log") errors -> $OUT/work/$name.log"
    grep 'error:' "$OUT/work/$name.log" | head -10
  fi
}
srcs commons-j7/src/main/java > "$OUT/work/commons.lst"
{ srcs thirddep-j7/src/main/java; srcs thirddep-j7/src/swt-win32/java; } > "$OUT/work/third.lst"
srcs xanderView/src/main/java > "$OUT/work/app.lst"
run commons 21 "$CP_COMMONS" "$OUT/commons" "$OUT/work/commons.lst"
run thirddep 21 "$CP_THIRD" "$OUT/third" "$OUT/work/third.lst"
run xanderView 21 "$CP_APP" "$OUT/app" "$OUT/work/app.lst"
exit $rc
