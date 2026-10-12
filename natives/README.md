# natives/ — provenance manifests of the native libraries

Design and platform matrix: [docs/NATIVES.md](../docs/NATIVES.md). No binaries live in git: each `<os>-<arch>.tsv`
lists, per file, where the upstream build comes from and what it must hash to. `tools/natives/bundle.sh` turns a
manifest into a bundle zip; the Maven build downloads one bundle per platform (hash pinned in the parent pom).

## Files
| file | platform | loader dir |
|---|---|---|
| `windows-x86_64.tsv` | Windows x64 | `libs/windows/amd64/` |
| `linux-x86_64.tsv` | Linux x64 (glibc ≥ 2.35) | `libs/linux/x86_64/` |
| `linux-aarch64.tsv` | Linux arm64 (glibc ≥ 2.35) | `libs/linux/aarch64/` |
| `macos-x86_64.tsv` | macOS 10.13+ Intel | `libs/macos/x86_64/` |
| `macos-aarch64.tsv` | macOS 11+ Apple Silicon | `libs/macos/aarch64/` |
| `target/` (gitignored) | bundler output: `<platform>/libs/…`, `xview-natives-<platform>-<rev>.zip` + `.sha256` | |

TSV columns (tab separated, `#` comments): `file  sha256  size  url  url_sha256  member  version  license  deps`
- `file`: the load-time file name in `libs/<os>/<arch>/` (Windows: `<loadname>.dll`; Unix: `lib<name>.so` / `.dylib` / `.jnilib`; dependencies keep their soname, e.g. `libhwy.so.1`).
- `url` + `url_sha256`: the upstream asset (jar, zip, 7z, NSIS exe, deb, dmg, wheel) and its hash; the bundler caches assets by hash in `~/.cache/xview-natives` (`XVIEW_NATIVES_CACHE`).
- `member`: path inside the asset; `!` separates nested layers (`swt.jar!swt-win32-4527.dll`, `data.tar.xz!./opt/...`, `….dmg` → `….pkg` → `Payload` → `Payload~` → file).
- `deps`: sibling libraries to load first (`natives.deps` in the bundle); `name?` = optional.
- Owner-built files are re-hosted as plain assets of the natives release (member `-`), e.g. `opencv_java460-windows-x86_64-custom.dll`; everything else points at the upstream asset.

## Commands
```bash
tools/natives/bundle.sh windows-x86_64 --rev 2026.10        # natives/target/windows-x86_64/ + zip
```
```bash
tools/natives/check-binaries.sh natives/target/linux-x86_64  # llvm-based import/export checks (CI)
```
```bash
./mvnw -B -DskipTests -Dxview.natives.dir="$PWD/natives/target/windows-x86_64" package   # dist zip from a local bundle
```

## Updating a native
1. Edit the TSV row (url, url_sha256, member, sha256, size, version, license). Hashes: `sha256sum` of the asset and of the extracted member.
2. `tools/natives/bundle.sh <platform> --rev <new rev>` for every platform; `check-binaries.sh` on the result.
3. Upload all `xview-natives-*-<rev>.zip` (+ `.sha256`, + re-hosted owner files) as assets of a new GitHub release `natives-<rev>` (create it as a draft, upload everything, then publish; the current one is `natives-2026.10`).
4. Set `xview.natives.rev` and the per-platform `xview.natives.sha256` values in the parent pom (`.zip.sha256` files).

## Hand-built overrides (owner)
Put the files into `X/libs/<os>/<arch>/`, run `sha256sum * > natives.sha256` there (or leave the manifest out for the
legacy exact-compare mode) and start with `-Dxview.libs.location=X`. Nothing else changes.

## Licenses
`THIRD-PARTY-NOTICES.md` in each bundle is generated from the `license` column. Redistributed: SWT (EPL-1.0 for 4.527,
EPL-2.0 for 3.135), 7-Zip-JBinding (LGPL-2.1-or-later + unRAR restriction), libjpeg-turbo (BSD-3-Clause/IJG/zlib),
webp-imageio (Apache-2.0) + libwebp (BSD-3-Clause), libavif/dav1d/aom (BSD-2-Clause; aom adds the AOM patent license),
libjxl/brotli/highway (BSD-3-Clause/MIT/Apache-2.0), OpenCV (Apache-2.0).
