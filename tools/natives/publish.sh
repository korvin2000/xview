#!/usr/bin/env bash
# Publishes the bundle zips of one revision as the GitHub release natives-<rev> of this repo and pins them in the pom
# (docs/NATIVES.md §3, natives/README.md "Updating a native"). Run after tools/natives/bundle.sh for all six platforms.
#
#   tools/natives/publish.sh <rev> [--dry-run]
#
# Needs: curl, python3, a GitHub token with repo scope in GITHUB_TOKEN or GH_TOKEN (never printed). The release is
# created as a draft, every natives/target/xview-natives-*-<rev>.zip (+ .sha256) is uploaded, then it is published.
# The parent pom gets xview.natives.rev = <rev> and, per platform, xview.natives.sha256 = the zip's hash.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
REV="${1:?rev required}"; DRY="${2:-}"
REPO="${XVIEW_REPO:-korvin2000/xview}"
TOKEN="${GITHUB_TOKEN:-${GH_TOKEN:-}}"; [ -n "$TOKEN" ] || { echo "GITHUB_TOKEN is required" >&2; exit 2; }
API="https://api.github.com/repos/$REPO"
OUT="$ROOT/natives/target"
PLATFORMS="windows-x86_64 windows-aarch64 linux-x86_64 linux-aarch64 macos-x86_64 macos-aarch64"

for p in $PLATFORMS; do
  [ -f "$OUT/xview-natives-$p-$REV.zip.sha256" ] || { echo "missing bundle for $p: run tools/natives/bundle.sh $p --rev $REV" >&2; exit 1; }
done

# pin the hashes in the pom (both the auto and the -Dswt.flavor profile of each platform carry the same value)
PY=$(command -v python3 || command -v python); "$PY" -I - "$ROOT/pom.xml" "$REV" "$OUT" $PLATFORMS <<'EOF'
import re, sys
pom, rev, out, *platforms = sys.argv[1:]
s = open(pom, encoding='utf-8').read()
s = re.sub(r'<xview\.natives\.rev>[^<]*</xview\.natives\.rev>', f'<xview.natives.rev>{rev}</xview.natives.rev>', s, count=1)
for p in platforms:
    sha = open(f'{out}/xview-natives-{p}-{rev}.zip.sha256', encoding='utf-8').read().split()[0]
    pat = re.compile(r'(<xview\.platform>' + re.escape(p) + r'</xview\.platform>.*?<xview\.natives\.sha256>)[0-9a-f]{64}(</xview\.natives\.sha256>)', re.S)
    s, n = pat.subn(lambda m: m.group(1) + sha + m.group(2), s)
    assert n == 2, f'{p}: expected 2 profiles, patched {n}'
open(pom, 'w', encoding='utf-8', newline='\n').write(s)
print('pom pinned to', rev)
EOF
[ "$DRY" = "--dry-run" ] && { echo "dry run: pom updated, nothing uploaded"; exit 0; }

gh() { curl -fsS -H "Authorization: Bearer $TOKEN" -H "Accept: application/vnd.github+json" "$@"; }
TAG="natives-$REV"
if ! gh "$API/releases/tags/$TAG" -o /dev/null 2>/dev/null; then
  gh -X POST "$API/releases" -d "{\"tag_name\":\"$TAG\",\"name\":\"$TAG\",\"draft\":true,\"body\":\"Native bundles rev $REV, built by tools/natives/bundle.sh from natives/<platform>.tsv (docs/NATIVES.md). The build downloads these only when the tracked natives/<os>/<arch>/ tree is absent.\"}" > "$OUT/release.json"
else
  gh "$API/releases/tags/$TAG" > "$OUT/release.json"
fi
ID="$("$PY" -I -c "import json,sys; print(json.load(open(sys.argv[1]))['id'])" "$OUT/release.json")"
UPLOAD="https://uploads.github.com/repos/$REPO/releases/$ID/assets"
for f in "$OUT"/xview-natives-*-"$REV".zip "$OUT"/xview-natives-*-"$REV".zip.sha256; do
  n="$(basename "$f")"; echo "uploading $n"
  gh -X POST -H "Content-Type: application/octet-stream" "$UPLOAD?name=$n" --data-binary @"$f" -o /dev/null
done
gh -X PATCH "$API/releases/$ID" -d '{"draft":false}' -o /dev/null
echo "published https://github.com/$REPO/releases/tag/$TAG"
