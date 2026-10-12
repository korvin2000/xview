#!/usr/bin/env bash
# Checks the natives of one bundle directory (natives/target/<platform>) with LLVM tools (docs/NATIVES.md §3):
#   - every shared library depends only on OS libraries, bundle siblings or (Windows) runtime DLLs the JDK ships;
#     CONCRT140 / Media Foundation imports (OpenCV) are reported as "needs VC++ redistributable / Media Foundation";
#   - the highest GLIBC_x.y version each Linux library needs, and the macOS minimum OS version;
#   - the JNI exports (Java_*) of each library are listed, so a vendored-Java mismatch is visible in the log.
# Needs llvm-readobj, llvm-objdump, llvm-nm (apt: llvm; brew: llvm; Windows: LLVM release). Exit 1 on a forbidden dependency.
set -euo pipefail
DIR="${1:?bundle dir (…/natives/target/<platform>)}"
LIBS="$(find "$DIR/libs" -mindepth 2 -maxdepth 2 -type d | head -1)"
[ -n "$LIBS" ] || { echo "no libs/<os>/<arch> under $DIR" >&2; exit 2; }
for t in llvm-readobj llvm-objdump llvm-nm; do command -v "$t" >/dev/null 2>&1 || { echo "$t not found (install llvm)" >&2; exit 2; }; done
rc=0
siblings="$(ls "$LIBS" | tr '\n' ' ')"
is_sibling() { case " $siblings " in *" $1 "*) return 0;; esac; return 1; }

for f in "$LIBS"/*; do
  name="$(basename "$f")"
  case "$name" in natives.sha256|natives.deps|*.md) continue;; esac
  echo "== $name"
  fmt="$(llvm-readobj --file-header "$f" 2>/dev/null | grep -E '^Format:' | head -1 || true)"
  case "$fmt" in
    *elf*|*ELF*)
      needed="$(llvm-readobj --needed-libs "$f" | sed -n '/NeededLibraries/,/]/p' | grep -vE 'NeededLibraries|\]' | tr -d ' ')"
      for n in $needed; do
        case "$n" in
          libc.so.*|libm.so.*|libpthread.so.*|libdl.so.*|librt.so.*|libutil.so.*|libgcc_s.so.*|libstdc++.so.*|libatomic.so.*|ld-linux*) ;;
          libgtk-3.so.*|libgdk-3.so.*|libglib-2.0.so.*|libgobject-2.0.so.*|libgio-2.0.so.*|libcairo.so.*|libpango*|libatk-1.0.so.*|libX11.so.*|libXtst.so.*|libgdk_pixbuf*|libharfbuzz*|libfontconfig*|libfreetype*|libz.so.*) echo "   system GTK/X11: $n";;
          *) if is_sibling "$n"; then echo "   sibling: $n"; else echo "   FORBIDDEN dependency: $n"; rc=1; fi;;
        esac
      done
      echo "   glibc floor: $(llvm-objdump -T "$f" | grep -oE 'GLIBC_[0-9.]+' | sort -t. -k2,2n -k3,3n -u | tail -1)  $(llvm-objdump -T "$f" | grep -oE 'GLIBCXX_[0-9.]+' | sort -t. -k3,3n -u | tail -1)"
      ;;
    *Mach-O*|*mach-o*)
      llvm-objdump --macho --dylibs-used "$f" | tail -n +2 | awk '{print $1}' | while read -r n; do
        case "$n" in
          /usr/lib/*|/System/Library/*) ;;
          @loader_path/*|@rpath/*) b="$(basename "$n")"; if is_sibling "$b"; then echo "   sibling: $n"; else echo "   FORBIDDEN dependency: $n"; exit 1; fi;;
          *) echo "   FORBIDDEN dependency: $n"; exit 1;;
        esac
      done || rc=1
      echo "   minos: $(llvm-objdump --macho --private-headers "$f" | grep -E 'minos|version ' | head -2 | tr -s ' ' | tr '\n' ' ')"
      ;;
    *COFF*|*coff*)
      llvm-readobj --coff-imports "$f" | grep -E '^\s*Name:' | awk '{print $2}' | while read -r n; do
        l="$(echo "$n" | tr 'A-Z' 'a-z')"
        case "$l" in
          kernel32.dll|user32.dll|gdi32.dll|advapi32.dll|ole32.dll|oleaut32.dll|shell32.dll|shlwapi.dll|comctl32.dll|comdlg32.dll|imm32.dll|usp10.dll|wininet.dll|winspool.drv|crypt32.dll|msvcrt.dll|api-ms-win-*|ucrtbase.dll|vcruntime140.dll|vcruntime140_1.dll|msvcp140.dll) ;;
          concrt140.dll) echo "   needs VC++ redistributable (not shipped by the JDK): $n";;
          mf.dll|mfplat.dll|mfreadwrite.dll) echo "   needs Media Foundation (absent on Windows N): $n";;
          *) if is_sibling "$n"; then echo "   sibling: $n"; else echo "   FORBIDDEN dependency: $n"; exit 1; fi;;
        esac
      done || rc=1
      ;;
    *) echo "   unknown format: $fmt"; rc=1;;
  esac
  jni="$(llvm-nm --extern-only --defined-only "$f" 2>/dev/null | grep -oE 'Java_[A-Za-z0-9_]+' | sort -u | wc -l | tr -d ' ')"
  echo "   JNI exports: $jni"
done
[ "$rc" = 0 ] && echo "check-binaries: OK" || echo "check-binaries: problems found" >&2
exit $rc
