# natives/ — the shipped native libraries and their provenance

Design and platform matrix: [docs/NATIVES.md](../docs/NATIVES.md). Each `<os>/<arch>/` directory **is** what the dist ships
as `libs/<os>/<arch>/`: the native files, `natives.sha256` (the loader's manifest), `natives.deps` (siblings to load
first) and `THIRD-PARTY-NOTICES.md`. Each `<os>-<arch>.tsv` says, per file, where the build comes from and what it must
hash to; `tools/natives/bundle.sh` rebuilds a tree from its TSV and zips it for the GitHub release `natives-<rev>`, which
the Maven build downloads only when the tree is absent.

## Files
| tree | manifest | platform |
|---|---|---|
| `windows/amd64/` | `windows-x86_64.tsv` | Windows x64 (AVX2 builds: Haswell / Zen 1 or newer) |
| `windows/aarch64/` | `windows-aarch64.tsv` | Windows 10+ arm64 |
| `linux/x86_64/` | `linux-x86_64.tsv` | Linux x64, glibc ≥ 2.35 (AVX2 builds of 7-Zip) |
| `linux/aarch64/` | `linux-aarch64.tsv` | Linux arm64, glibc ≥ 2.35 |
| `macos/x86_64/` | `macos-x86_64.tsv` | macOS 10.15+ Intel |
| `macos/aarch64/` | `macos-aarch64.tsv` | macOS 11+ Apple Silicon |
| `research/` | | everything found during the P7 research: `SOURCES.md` (all sources, alternatives, gaps), `hashes-2026-10.txt`, the reviewed plan |
| `target/` (gitignored) | | bundler output: `xview-natives-<platform>-<rev>.zip` + `.sha256` |

TSV columns (tab separated, `#` comments): `file  sha256  size  url  url_sha256  member  version  license  deps`
- `file`: the load-time file name (Windows: `<loadname>.dll`; Unix: `lib<name>.so` / `.dylib` / `.jnilib`; dependencies keep their soname, e.g. `libhwy.so.1`).
- `url` + `url_sha256`: the upstream asset (jar, zip, 7z, NSIS exe, deb, dmg, wheel) and its hash; the bundler caches assets by hash in `~/.cache/xview-natives` (`XVIEW_NATIVES_CACHE`), so rebundling works offline once the cache is filled.
- `url` = `-`: an owner-built file that exists only in the tree (`libjxl_jni.dll`, `libavif.dll`, `opencv_java460.dll` on Windows x64); the bundler takes it from the tree and verifies the hash.
- `member`: path inside the asset; `!` separates nested layers (`….zip` → `lib/….jar!Windows-amd64/lib7-Zip-JBinding.dll`, `data.tar.xz!./opt/...`, `….dmg` → `….pkg` → `Payload` → `Payload~` → file); `-` = the asset is the file.
- `deps`: sibling libraries to load first (`natives.deps`); `name?` = optional.

## Commands
```bash
tools/natives/bundle.sh windows-x86_64 --rev 2026.10.1        # rewrites natives/windows/amd64/ + natives/target/…zip
```
```bash
tools/natives/check-binaries.sh natives/linux/x86_64            # llvm-based import/export checks (CI)
```
```bash
./mvnw -B -DskipTests package                                   # uses the tracked tree; downloads the bundle only without it
```

## Updating a native
1. Edit the TSV row (url, url_sha256, member, sha256, size, version, license). Hashes: `sha256sum` of the asset and of the extracted member. An owner-built file: copy it into `<os>/<arch>/`, url `-`.
2. `tools/natives/bundle.sh <platform> --rev <new rev>` for every platform; `check-binaries.sh` on the trees; commit the trees.
3. Upload all `xview-natives-*-<rev>.zip` (+ `.sha256`) as assets of a new GitHub release `natives-<rev>` (draft, upload everything, publish).
4. Set `xview.natives.rev` and the per-platform `xview.natives.sha256` values in the parent pom (`.zip.sha256` files).

## Hand-built overrides (owner)
Put the files into `X/libs/<os>/<arch>/`, run `sha256sum * > natives.sha256` there (or leave the manifest out for the
legacy exact-compare mode) and start with `-Dxview.libs.location=X`. Nothing else changes.

## Licenses
`THIRD-PARTY-NOTICES.md` in each tree is generated from the `license` column. Redistributed: SWT (EPL-2.0), 7-Zip-JBinding
(LGPL-2.1-or-later + unRAR restriction), libjpeg-turbo (BSD-3-Clause/IJG/zlib), webp4j (MIT) + libwebp (BSD-3-Clause),
libavif/dav1d/aom (BSD-2-Clause; aom adds the AOM patent license), libjxl/brotli/highway (BSD-3-Clause/MIT/Apache-2.0),
OpenCV (Apache-2.0).
