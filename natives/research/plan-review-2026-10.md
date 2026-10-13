# xanderView natives: cross-platform packaging plan (rev 2, 2026-10-13, after Opus review)

Context: xanderView is a Java 26 desktop image viewer (SWT shell + embedded AWT canvas). Decoding speed comes from
native libraries loaded through ONE custom loader, `net.j7.commons.jni.LibraryLoader` (commons-j7). Today only
Windows x64 natives exist, outside git, at `C:\Program Files\xview\libs\windows\amd64\` (hard-coded in
`commons-j7/src/main/resources/libs.properties`), copied once into a per-user cache `%APPDATA%\xanderView\libs\windows\amd64\`
and loaded from there. Goals: 6 platforms (windows/linux/macos x x86_64/aarch64), prebuilt natives (no own compilation
where avoidable), automated per-platform packaging, installer support via the owner's `jrunner`
(github.com/korvin2000/java-runner), contributor-friendly repo, fast start kept.

## 1. Facts established (2026-10-12/13)

### 1.1 What the 8 Windows x64 natives really are (SHA-256 compared)
| file | truth | imports beyond KERNEL32 |
|---|---|---|
| `swt.dll` 644 KB | byte-identical to stock Eclipse 4.5 `swt-win32-4527.dll` (renamed) | Win32 system DLLs only |
| `lib7-Zip-JBinding.dll` 5.5 MB | byte-identical to Maven Central `net.sf.sevenzipjbinding:sevenzipjbinding-all-platforms:16.02-2.01` `Windows-amd64/` | msvcrt, OLEAUT32, USER32 (system) |
| `webp-imageio.dll` 889 KB | byte-identical to the `native/Windows/x86_64/` file of a webp-imageio fork set (owner's `xanderView/lib/libwebp-imageio.7z`, 6/6 platforms) | msvcrt |
| `turbojpeg.dll` 973 KB | owner's VS2015 build of libjpeg-turbo 3.0.91 (3.1 beta snapshot), JNI Java API | none |
| `opencv_java460.dll` 3.4 MB | custom minimal build (WPILib `opencv-jni:4.6.0-5:windowsx86-64` is 13.9 MB, same import set) | **CONCRT140, MSVCP140, VCRUNTIME140(_1), UCRT, MF/MFPlat/MFReadWrite** |
| `libavif.dll` 3.9 MB | custom MinGW build, libavif 1.1.1, codecs static; JNA binding generated from 1.0.3 headers | msvcrt |
| `libjxl_jni.dll` 1.5 MB | self-contained; equivalent upstream: libjxl v0.12.0 `jxl-x64-windows-static.7z` `lib/jxl_jni.dll` (same 2 exports, KERNEL32 only) | none |
| `libgcc_s_seh-1.dll` 80 KB | MinGW runtime; nothing imports it; loaded explicitly by the vendored 7-Zip init on every OS (bug on Linux/macOS) | — |

JDK 26 `bin/` ships `msvcp140`, `vcruntime140`, `vcruntime140_1`, `ucrtbase` but **not `concrt140.dll`**: OpenCV
(custom and WPILib alike) fails on a Windows without the VC++ redistributable, and Media Foundation is absent on
Windows N editions. GitHub Windows runners have the redistributable, so a probe there would pass falsely.

Conclusion: the valuable part of the design is (a) one loader, (b) persistent per-user cache, (c) per-platform
packages instead of fat jars, (d) the small classes-only OpenCV jar. "Custom optimized builds" holds only for
OpenCV, libavif and turbojpeg, and no benchmark against the upstream builds exists. The plan keeps an override slot
for hand-built files (`-Dxview.libs.location`) and takes everything else from upstream.

### 1.2 Prebuilt availability (verified by download + symbol/import inspection unless marked (C) = Codex/web only)
| library | win-x86_64 | win-aarch64 | linux-x86_64 | linux-aarch64 | macos-x86_64 | macos-aarch64 | source |
|---|---|---|---|---|---|---|---|
| SWT 3.135.0 stock | yes | yes | yes | yes | yes | yes | Maven Central `org.eclipse.platform:org.eclipse.swt.<ws>.<os>.<arch>:3.135.0` (`libswt-*-4974r10.so`, `.jnilib`, `swt-*-win32-4974r10.dll`); win-x86_64 today = vendored 4.527 Java + renamed stock `swt-win32-4527.dll` |
| 7-Zip-JBinding 16.02-2.01 (27 JNI exports) | yes | **no** (only 26.03-2.3 exists, incompatible JNI) | yes | yes: Central `sevenzipjbinding-linux-arm64:16.02-2.01` (27 exports, glibc 2.17) | yes | yes: muCommander patched all-platforms jar, `Mac-aarch64/` (28 exports, system libs only) | all self-contained |
| TurboJPEG (libjpeg-turbo) | 3.0.4 JNI / 3.2.0 C API | 3.2.0 C API only (`vc-arm64.exe`) | 3.0.4 JNI (`amd64.deb`) / 3.2.0 | 3.0.4 JNI (`arm64.deb`) / 3.2.0 | 3.0.4 JNI universal dylib / 3.2.0 universal | same | github releases. **3.1+ official binaries have no JNI exports (Java API became JNA)**; 3.0.4 JNI set differs from the vendored 3.0.91 Java in 6 compressor/ICC names. The `tj3*` C API is in every 3.x build (owner's DLL included). |
| webp-imageio (`com.luciad.imageio.webp`, 76 exports, sets equal) | yes | no | yes | yes | yes | yes | owner's fork set; Maven artifact id (C) |
| libavif (JNA `avif`, API >= 1.0.3) | custom only | (C) | (C) | (C) | (C) | (C) | distro/brew builds need sibling dav1d/aom; conda-forge ships all 6 with siblings (C) |
| libjxl `jxl_jni` (2 exports) | yes (0.12.0 static, self-contained) | **no** | yes: Debian 13 `libjpegxl-java` 0.11.2 (needs siblings `libbrotlidec.so.1`, `libhwy.so.1`, `libjxl_threads.so.0.11`; glibc >= 2.35) | yes (same) | **no** (Homebrew/MacPorts: JNI off) | **no** | Linux also Arch/Ubuntu; macOS/win-arm64 need a CI build or a pure-Java fallback (jxlatte) |
| OpenCV `opencv_java460` | WPILib 13.9 MB (or custom 3.4 MB) | WPILib | WPILib (glibc 2.34, libstdc++) | WPILib | WPILib (system frameworks) | WPILib | `edu.wpi.first.thirdparty.frc2023.opencv:opencv-jni:4.6.0-5:<classifier>`; Windows needs `concrt140.dll` shipped app-locally |
| `concrt140.dll` | VC++ redistributable (redistributable per Microsoft's list) | same | — | — | — | — | ship next to OpenCV on Windows |

Toolchain facts: GitHub-hosted `ubuntu-24.04-arm`, `windows-11-arm`, `macos-15-intel`, `macos-14/15` (arm64) runners
are free for public repos. GitHub LFS: 10 GiB storage + 10 GiB/month, metered; forks' pulls count against the owner.
The stock SWT 3.135 win32 `SWT_AWT` still reflects into `sun.awt.windows.WEmbeddedFrame`, so `Add-Exports` stays
required on Windows even after a stock-SWT migration.

### 1.3 LibraryLoader: critical review (578 LOC)
Strengths: single choke point; persistent per-user cache; atomic temp-file publish; exact content check picks up a
replaced source DLL; soft-fail + `tryLoadLibrary` for fail-fast callers; `file:`/`jar:`/`http:` sources; Unix `lib`
prefix and macOS `.dylib`/`.jnilib` candidates; `arm64`->`aarch64`.
Weaknesses:
1. No version/hash metadata: the up-to-date check reads every native twice per start (fine locally, 21-28 ms warm;
   impractical for `jar:` and `http:` sources), and the cache is keyed by file name only, so several installed
   versions (the owner has `xanderview.jar.2025/.2026/.GOOD` next to each other) fight over one cache file.
2. `libs.properties` bakes `C:/Program Files/xview` into the jar; no distributable default ("`libs/` next to the jar").
3. Soft-fail + late `UnsatisfiedLinkError` (PLAN B10); no startup summary, no `--check-natives`.
4. Sibling dependencies: only a Windows map (`libavif` -> `libgcc_s_seh-1`). `System.load` sets no search path, so a
   dependency must be loaded first by absolute path (then the dynamic linker matches it by module name / soname /
   install name). The plan generalizes this to a per-bundle dependency list.
5. Exceptions to "one loader": JNA extracts `jnidispatch` itself; lz4-java extracts an xxHash JNI itself
   (`Hasher` only needs xxHash64 on 3 KB: `XXHashFactory.safeInstance()` gives identical output without natives).
6. `appName`/cache layout hard-coded; Linux data dir `~/.local/share/.xanderView` (dot), no XDG; macOS fallback uses
   `$USER` as a path. Fix before the first non-Windows release (the same function places user data).
7. `SevenZip.ensureLibraryIsInitialized` loads `libgcc_s_seh-1` on every OS (warning + "missing" on Linux/macOS).
8. No cross-process lock (documented, acceptable).

## 2. Decisions (rev 2)

D1 **Binaries outside git; one hash-pinned bundle per platform.** The repo holds, per platform, a provenance
manifest `natives/<os>-<arch>.tsv` (file, sha256, size, upstream URL, member path in the archive, version, license,
load dependencies). A maintainer/CI job (`tools/natives/bundle.sh`, run by `natives-bundle.yml`) fetches the
upstream files, verifies them, extracts (`7z`, `dpkg-deb`, `pkgutil`/`7z` for .pkg), checks self-containment and
exports with `llvm-readobj`/`llvm-objdump`/`llvm-nm` (glibc floor, macOS minos, Windows import whitelist = what
the JDK ships + OS DLLs), generates `THIRD-PARTY-NOTICES.md`, and publishes `xview-natives-<platform>-<rev>.zip`
(+ `.sha256`) as assets of an immutable GitHub Release `natives-<rev>` on this repo. The contributor build fetches
exactly one zip through `download-maven-plugin` (sha256 pinned in the pom, cached in `~/.m2`, honors Maven
proxies/offline settings) or uses a local tree via `-Dxview.natives.dir=<dir>`. Rejected: Git LFS (client
requirement, quota shared with forks), committing binaries (repo bloat), GitHub Packages (needs auth), a 48-entry
Java fetcher in every contributor build (multiple upstream hosts, ignores Maven settings).

D2 **Bundle contents = the runtime contract.** `libs/<os>/<arch>/<files>` plus `natives.sha256` (plain `sha256sum`
format: hex, two spaces, file name; verifiable with `sha256sum -c`) and `natives.deps` (one line per library that
needs siblings: `libjxl_jni.so: libbrotlidec.so.1 libhwy.so.1 libjxl_threads.so.0.11`, load order left to right).
The build copies `natives.sha256` and `natives.deps` into the app jar as `/libs/<os>/<arch>/…` resources, so the jar
knows which natives it was built with (expected set, versions for diagnostics, trust anchor for non-local sources).

D3 **Loader: content-addressed cache, legacy path kept.**
- Source manifest present (`<location>/libs/<os>/<arch>/natives.sha256`): bundle id = first 12 hex of
  sha256(manifest text); cache dir `<AppData>/libs/<os>/<arch>/<bundle12>/`. Each library is still copied lazily on
  first use: temp file -> sha256 verified against the manifest -> atomic move; a file already present in the bundle
  dir is trusted (one `stat` per library per start, no content compare). Bundle dirs are immutable, so several
  installed versions coexist and the rename-old dance is unnecessary; dirs unused for 30 days (marker file) are
  swept. If the source manifest differs from the jar's copy, a warning names the override (the owner's hand-built
  files workflow: drop files into a directory with a regenerated `natives.sha256`, point `-Dxview.libs.location`
  at it).
- No source manifest: today's flat directory + exact content compare, unchanged (keeps the deployed
  `C:\Program Files\xview\libs` working as is).
- Default `location` when nothing is configured: the directory of the jar if it contains `libs/`, else inside the
  jar (`jar:`). `libs.properties` loses the absolute path.
- Dependencies from `natives.deps` (plus the existing Windows map, extended with `opencv_java460 -> concrt140`) are
  copied and `System.load`-ed first, by absolute path.
- macOS: after copying, delete the `com.apple.quarantine` extended attribute (Java's `Files.copy` may clonefile and
  keep it).
- Not done (cut after review): in-place loading flag, in-jar packaging mode, promotion of `http:` sources.
- Contract changes go into `.claude/rules/commons.md`, `CLAUDE.md` (natives bullet), ARCHITECTURE §6.

D4 **Startup diagnostics (B10):** `NativeStatus` logs one line per expected native (from the in-jar manifest:
loaded / missing / bundle id). Only SWT is fatal; everything else soft-fails as today. `--check-natives` loads all
expected natives headless and exits non-zero if any is missing (used by the CI probe).

D5 **Packaging stays in the app module.** The `swt-*` profiles gain `xview.platform` (`windows-x86_64`, …) and
`xview.libdir` (`windows/amd64`, `linux/x86_64`, `macos/aarch64`, …: the loader's existing names, Windows `amd64`
quirk included). `download-maven-plugin` fetches the platform bundle into `xanderView/target/natives/` in
`generate-resources`; the two manifest files are added as resources; one `maven-assembly-plugin` execution builds
`xanderview-<version>-<platform>.zip` = `xanderview.jar`, `libs/<os>/<arch>/…`, `jrunner.json` (filtered per
platform), launcher script, `LICENSE`, `THIRD-PARTY-NOTICES.md`. Cross-building all 6 zips on one machine works
because Java is portable and the natives are prebuilt (`-Dswt.flavor=…`). **Windows aarch64 is deferred** until
more than SWT, OpenCV and TurboJPEG exist for it (no 7-Zip, WebP, JXL today).

D6 **CI (GitHub Actions):** `build.yml`: one ubuntu job builds the 5 zips (6 when win-arm64 is enabled) and
uploads them as artifacts; a probe matrix (`windows-latest`, `ubuntu-24.04`, `ubuntu-24.04-arm`, `macos-15-intel`,
`macos-14`) runs `java -jar … --check-natives` plus one decode per codec from a cold cache, with the VC++
redistributable removed on the Windows runner (or `concrt140` checked explicitly). `release.yml` attaches the zips
and jrunner launchers. `natives-bundle.yml` (manual) runs the bundler (D1). `natives-build.yml` (manual, later)
compiles only what has no prebuilt source (currently `jxl_jni` for macOS, maybe libavif) and feeds the bundler.

D7 **jrunner:** jrunner takes one `jar` and copies `files` with their path relative to the config, and `collectFiles`
runs once for all targets, so a shared config cannot describe 6 different jars. Maven therefore writes a filtered
`jrunner.json` into each platform's dist directory (`jar: xanderview.jar`, `files: ["libs"]`, per-platform
`update.url`), and `jrunner build --target <platform>` runs there. Suggested jrunner improvements (owner's repo):
per-target `jar`/`files` (or `{platform}` placeholders) with per-target update packages even without a bundled
runtime; strip `com.apple.quarantine` on macOS installs; document that macOS 15 removed the Control-click bypass.
Longer term: a platform-neutral app jar + per-flavor `lib/swt-<flavor>.jar` via `Class-Path` would allow one jar
for all platforms.

D8 **Library-level choices**
- **TurboJPEG: replace the vendored JNI Java API with a small FFM binding** (`tj3Init`, `tj3Set`, `tj3DecompressHeader`,
  `tj3GetScalingFactors`, `tj3Decompress8`, `tj3Destroy`, `tj3GetErrorStr`; ~200 lines in the app). Java 26 has FFM,
  `Enable-Native-Access` is already in the manifest, `Buf` is already an mmapped segment (no copy in), and
  `Linker.Option.critical(true)` lets the decoder write straight into the heap `int[]` of the BufferedImage like
  JNI's critical arrays do. Any official 3.x build then works, including 3.2.0 for Windows arm64, and the owner's
  3.0.91 DLL too. Benchmark against the current path before switching; fallback: vendored Java aligned to 3.0.4 +
  official 3.0.4 natives (5/6 platforms).
- **OpenCV:** WPILib `opencv-jni` 4.6.0-5 on all platforms, including Windows x64 (consistency; the custom 3.4 MB
  build stays available as an override), plus `concrt140.dll` shipped app-locally on Windows. Document the Media
  Foundation requirement (Windows N: Media Feature Pack). Longer term: a static-CRT build without videoio.
- **SWT:** keep vendored 4.527 + `swt.dll` on Windows x64 in this round; stock 3.135.0 from Central elsewhere.
  P4.2 (Windows x64 to the stock overlay) is a separate step; it unifies the flavors and deletes 657 vendored files
  but does not remove the `Add-Exports` need.
- **7-Zip-JBinding:** Central all-platforms jar (win-x64, linux-x64, macos-x64) + Central `linux-arm64` +
  muCommander `Mac-aarch64`. The vendored init loads `libgcc_s_seh-1` only on Windows (patch).
- **WebP:** the fork set (6/6; artifact id pending (C)).
- **JXL:** Windows x64 static `jxl_jni.dll`; Linux x64/arm64 Debian `libjpegxl-java` + its 3 sibling libraries via
  `natives.deps` (glibc >= 2.35), or a static CI build; macOS: CI build (`natives-build.yml`) or pure-Java
  `jxlatte` fallback; until then JXL soft-fails there.
- **libavif:** pending (C); conda-forge (all 6, with sibling `libdav1d`/`libaom`) is the leading ready-made candidate.
- **lz4:** `XXHashFactory.safeInstance()` (same xxHash64 output, no Unsafe, no native extraction).
- **JNA:** optionally `jna.boot.library.path` to the bundle dir + `jnidispatch` as a managed native (later).

D9 **Licensing and signing.** The repo has no LICENSE (owner decision needed). Re-hosting binaries is
redistribution: 7-Zip-JBinding LGPL-2.1 + unRAR restriction, libgcc GPL with runtime exception, SWT EPL-2.0 (source
pointer), libjpeg-turbo IJG/BSD acknowledgement, OpenCV Apache-2.0 + NOTICE, libjxl/libavif BSD, libaom patent
license if linked. The manifest's license column drives a generated `THIRD-PARTY-NOTICES.md`; the bundler fails
when a license is missing. Code signing (Windows SmartScreen/Smart App Control, macOS notarization) needs a budget
decision; unsigned natives work on Windows 10/11 default settings and on macOS after quarantine removal.

## 3. Directory layout (after the change)
```
natives/
  README.md                         design, how to add/update a native, provenance rules, override workflow
  windows-x86_64.tsv                file  sha256  size  url  member  version  license  deps
  linux-x86_64.tsv  linux-aarch64.tsv  macos-x86_64.tsv  macos-aarch64.tsv  (windows-aarch64.tsv later)
tools/natives/bundle.sh             fetch + verify + extract + notices + zip (maintainer/CI)
tools/natives/check-binaries.sh     llvm-readobj/objdump/nm checks (CI)
xanderView/src/assembly/dist.xml    per-platform zip descriptor
xanderView/src/dist/                launcher scripts, jrunner.json template (filtered)
.github/workflows/{build,release,natives-bundle,natives-build}.yml
```
Bundle (`xview-natives-<platform>-<rev>.zip`) and dist zip layout:
```
libs/<os>/<arch>/<files>  libs/<os>/<arch>/natives.sha256  libs/<os>/<arch>/natives.deps  THIRD-PARTY-NOTICES.md
```
Cache: `<AppData>/libs/<os>/<arch>/<bundle12>/<files>` (manifest mode) or `<AppData>/libs/<os>/<arch>/<files>` (legacy).

## 4. Steps (PLAN P7)
- P7.1 Manifests (`natives/*.tsv`) for the 5 platforms from the verified sources; `bundle.sh` + `check-binaries.sh`;
  first bundles built locally for windows-x86_64 and uploaded to `natives-2026.10`.
- P7.2 Loader: content-addressed cache + legacy path, default location, `natives.deps`, `concrt140`, quarantine
  xattr, `NativeStatus` + `--check-natives`, `libs.properties` without the absolute path, lz4 `safeInstance`,
  7-Zip `libgcc` Windows-only. Verify on Windows: cold cache, second start (one stat per library), override dir,
  legacy dir, missing native.
- P7.3 Maven: `xview.platform`/`xview.libdir`, `download-maven-plugin`, assembly, filtered `jrunner.json`; 5 dist
  zips built on Windows via `-Dswt.flavor`.
- P7.4 TurboJPEG FFM binding + benchmark; remove the vendored `org.libjpegturbo.turbojpeg` if it wins.
- P7.5 GitHub Actions: build, probe matrix, release, natives-bundle.
- P7.6 jrunner: per-platform config in dist; proposed jrunner changes filed in its repo.
- P7.7 `natives-build.yml` for `jxl_jni` macOS (and libavif if nothing prebuilt qualifies).
- P7.8 Docs: DEPENDENCIES §4 around the manifests, BUILD (dist, platforms), ARCHITECTURE §6, SPEC (files on disk),
  SOURCE_MAP, `.claude/rules/commons.md`, CLAUDE.md natives bullet, PLAN ticks; `tools/check-docs.sh`.

## 5. Open questions for the owner
1. Is `korvin2000/xview` public? (Release downloads for forks; free arm runners.)
2. LICENSE for the repo itself; is redistributing LGPL/EPL binaries with notices acceptable?
3. OpenCV Windows x64: WPILib build as default (proposed) or keep the custom 3.4 MB DLL?
4. TurboJPEG via FFM (proposed, needs a benchmark) or JNI 3.0.4 (no Windows arm64)?
5. SWT Windows x64: migrate to stock 3.135 in this round or later (proposed: later, P4.2)?
6. Which Java runtime to ship with jrunner (JDK 26 is non-LTS; 25 LTS or 27)?
7. Signing budget (Windows Authenticode / Apple Developer ID)?
8. Security: the git remote URL in `.git/config` embeds a GitHub personal access token in plain text. Rotate it
   and use a credential helper.
