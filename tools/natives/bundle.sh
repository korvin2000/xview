#!/usr/bin/env bash
# Builds the native bundle of one platform from natives/<platform>.tsv (docs/NATIVES.md §3).
#
#   tools/natives/bundle.sh <platform> [--rev <rev>] [--out <dir>] [--cache <dir>] [--no-zip]
#
# platform: windows-x86_64 | linux-x86_64 | linux-aarch64 | macos-x86_64 | macos-aarch64 | windows-aarch64
# For every TSV row the upstream asset is downloaded into the cache (content-addressed by its SHA-256, so a second run
# is offline), verified, the member is extracted through the layers separated by '!' (zip/jar/7z/exe/deb/dmg/pkg/
# cpio/whl, all via 7-Zip), renamed to the load-time file name and verified against the row's SHA-256. The output is
#   <out>/<platform>/libs/<os>/<arch>/<files> + natives.sha256 + natives.deps, <out>/<platform>/THIRD-PARTY-NOTICES.md
#   <out>/xview-natives-<platform>-<rev>.zip (+ .sha256)
# Tools: bash, curl, sha256sum, 7z (7z | 7zz | "C:\Program Files\7-Zip\7z.exe"). Timestamps inside the zip are fixed
# to SOURCE_DATE_EPOCH (default 2026-01-01) for reproducible archives.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PLATFORM="${1:?platform required}"; shift
REV="dev"; OUT="$ROOT/natives/target"; CACHE="${XVIEW_NATIVES_CACHE:-$HOME/.cache/xview-natives}"; ZIP=1
while [ $# -gt 0 ]; do
  case "$1" in
    --rev) REV="$2"; shift 2;;
    --out) OUT="$2"; shift 2;;
    --cache) CACHE="$2"; shift 2;;
    --no-zip) ZIP=0; shift;;
    *) echo "unknown option $1" >&2; exit 2;;
  esac
done

case "$PLATFORM" in
  windows-x86_64) LIBDIR=windows/amd64;;
  windows-aarch64) LIBDIR=windows/aarch64;;
  linux-x86_64) LIBDIR=linux/x86_64;;
  linux-aarch64) LIBDIR=linux/aarch64;;
  macos-x86_64) LIBDIR=macos/x86_64;;
  macos-aarch64) LIBDIR=macos/aarch64;;
  *) echo "unknown platform $PLATFORM" >&2; exit 2;;
esac
TSV="$ROOT/natives/$PLATFORM.tsv"
[ -f "$TSV" ] || { echo "missing $TSV" >&2; exit 2; }

if command -v 7z >/dev/null 2>&1; then SEVENZ=7z
elif command -v 7zz >/dev/null 2>&1; then SEVENZ=7zz
elif [ -x "/c/Program Files/7-Zip/7z.exe" ]; then SEVENZ="/c/Program Files/7-Zip/7z.exe"
else echo "7-Zip (7z) is required" >&2; exit 2; fi

sha() { sha256sum "$1" | cut -c1-64; }
export SOURCE_DATE_EPOCH="${SOURCE_DATE_EPOCH:-1767225600}"

DEST="$OUT/$PLATFORM"; LIBS="$DEST/libs/$LIBDIR"
rm -rf "$DEST"; mkdir -p "$LIBS" "$CACHE"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT

# fetch <url> <sha256> -> prints the cached asset path
fetch() {
  local url="$1" want="$2" dst="$CACHE/$2"
  if [ ! -f "$dst" ]; then
    echo "  downloading $url"
    curl -fsSL --retry 3 -o "$dst.part" "$url"
    local got; got="$(sha "$dst.part")"
    [ "$got" = "$want" ] || { echo "  checksum mismatch for $url: expected $want, got $got" >&2; rm -f "$dst.part"; exit 1; }
    mv "$dst.part" "$dst"
  fi
  printf '%s' "$dst"
}

# extract <archive> <member-chain> <outfile>: walks the '!'-separated layers with 7-Zip
extract() {
  local archive="$1" chain="$2" outfile="$3" layer rest dir n=0 member found
  rest="$chain"
  while [ -n "$rest" ]; do
    layer="${rest%%!*}"; [ "$layer" = "$rest" ] && rest="" || rest="${rest#*!}"
    n=$((n+1)); dir="$WORK/l$n"; rm -rf "$dir"; mkdir -p "$dir"
    member="${layer#./}"; found=""
    # tar members may be stored with a leading ./ ; try the exact names first, then unpack everything
    for try in "$member" "./$member"; do
      "$SEVENZ" x -y -o"$dir" "$archive" "$try" >/dev/null 2>&1 || true
      if [ -e "$dir/$member" ]; then found="$dir/$member"; break; fi
    done
    if [ -z "$found" ]; then
      "$SEVENZ" x -y -o"$dir" "$archive" >/dev/null 2>&1 || true   # symlink errors on Windows are tolerated
      for cand in "$dir/$member" "$dir/${member%.xz}" "$dir/${member%.gz}" "$dir/${member%.zst}"; do
        if [ -e "$cand" ]; then found="$cand"; break; fi
      done
      [ -n "$found" ] || { echo "  member $layer not found in $archive" >&2; exit 1; }
    fi
    archive="$found"
    # a compressed single file (data.tar.xz) must be unpacked before the next layer looks inside it
    case "$archive" in
      *.xz|*.gz|*.zst|*.lz) if [ -n "$rest" ]; then "$SEVENZ" x -y -o"$dir/u" "$archive" >/dev/null; archive="$(find "$dir/u" -type f | head -1)"; fi;;
    esac
  done
  cp "$archive" "$outfile"
}

echo "bundle $PLATFORM ($LIBDIR) from $TSV"
: > "$LIBS/natives.sha256"; : > "$LIBS/natives.deps"
NOTICES="$DEST/THIRD-PARTY-NOTICES.md"
{
  echo "# Third-party native libraries: $PLATFORM"
  echo
  echo "Bundle rev $REV, built by tools/natives/bundle.sh from natives/$PLATFORM.tsv. Each file is an unmodified upstream build (renamed to its load-time name where noted)."
  echo
  echo "| file | version | license | upstream |"
  echo "|---|---|---|---|"
} > "$NOTICES"
while IFS=$'\t' read -r file sha256 size url url_sha256 member version license deps; do
  case "$file" in ''|'#'*) continue;; esac
  echo "- $file ($version)"
  asset="$(fetch "$url" "$url_sha256")"
  if [ -z "${member:-}" ] || [ "$member" = "-" ]; then cp "$asset" "$LIBS/$file"; else extract "$asset" "$member" "$LIBS/$file"; fi
  got="$(sha "$LIBS/$file")"
  [ "$got" = "$sha256" ] || { echo "  checksum mismatch for $file: expected $sha256, got $got" >&2; exit 1; }
  [ "$(stat -c %s "$LIBS/$file")" = "$size" ] || { echo "  size mismatch for $file" >&2; exit 1; }
  echo "$sha256  $file" >> "$LIBS/natives.sha256"
  [ -n "${deps:-}" ] && echo "$file: $deps" >> "$LIBS/natives.deps"
  echo "| \`$file\` | $version | $license | $url (\`$member\`) |" >> "$NOTICES"
done < "$TSV"
[ -s "$LIBS/natives.deps" ] || rm -f "$LIBS/natives.deps"
cp "$NOTICES" "$LIBS/../../../THIRD-PARTY-NOTICES.md" 2>/dev/null || true
BUNDLE_ID="$(sha "$LIBS/natives.sha256" | cut -c1-12)"
echo "bundle id $BUNDLE_ID, $(wc -l < "$LIBS/natives.sha256") files in $LIBS"

if [ "$ZIP" = 1 ]; then
  find "$DEST" -exec touch -d "@$SOURCE_DATE_EPOCH" {} +
  ZIPFILE="$OUT/xview-natives-$PLATFORM-$REV.zip"; rm -f "$ZIPFILE"
  (cd "$DEST" && "$SEVENZ" a -tzip -mx=9 -mtc=off -r "$ZIPFILE" libs THIRD-PARTY-NOTICES.md >/dev/null)
  sha "$ZIPFILE" > "$ZIPFILE.sha256"
  echo "wrote $ZIPFILE ($(stat -c %s "$ZIPFILE") bytes, sha256 $(cat "$ZIPFILE.sha256"))"
fi
