#!/usr/bin/env bash
# Doc-drift check (see CLAUDE.md "Doc sync"):
#  1. every app class under xanderView/src/main/java is linked from docs/SOURCE_MAP.md
#  2. every relative markdown link in the project docs points to an existing file/dir
# Usage: bash tools/check-docs.sh    (exit 1 when drift is found)
set -uo pipefail
cd "$(dirname "$0")/.."
issues=0

# 1. classes missing from SOURCE_MAP
while read -r f; do
  name=$(basename "$f" .java)
  grep -q "\[$name\](" docs/SOURCE_MAP.md || { echo "SOURCE_MAP missing: $f"; issues=$((issues+1)); }
done < <(git ls-files --cached --others --exclude-standard -- 'xanderView/src/main/java/*.java')

# 2. broken relative links
docs=$(ls ./*.md docs/*.md .claude/rules/*.md 2>/dev/null)
for md in $docs; do
  dir=$(dirname "$md")
  grep -o '\](\([^)]*\))' "$md" | sed -E 's/^\]\((.*)\)$/\1/' | while read -r link; do
    case "$link" in http*|mailto:*|\#*) continue ;; esac
    target="${link%%#*}"
    [[ -z "$target" ]] && continue
    [[ -e "$dir/$target" ]] || echo "BROKEN link in $md: $link"
  done
done > /tmp/xview-check-docs.$$ 2>&1
if [[ -s /tmp/xview-check-docs.$$ ]]; then cat /tmp/xview-check-docs.$$; issues=$((issues+$(wc -l < /tmp/xview-check-docs.$$))); fi
rm -f /tmp/xview-check-docs.$$

if (( issues )); then echo "check-docs: $issues issue(s)"; exit 1; fi
echo "check-docs: OK"
