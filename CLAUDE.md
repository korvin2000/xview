# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**xanderView** is a desktop image viewer and organizer. It is keyboard-driven, built with Java 26, and uses an SWT shell around an embedded AWT canvas. You browse a folder or archive at full screen. The app scales and rotates each image automatically and remembers your per-image adjustments. One keystroke copies the current image into a target folder; copies are deduplicated by content hash and can optionally be AES-encrypted.
Platforms:
- **Works today:** Windows x64.
- **Target:** Windows, Linux and macOS on x86_64, with aarch64 optional (PLAN P5).

Decoding speed comes from **custom-built native libraries** (JNI/JNA). They are loaded through **vendored, patched** SWT, 7-Zip and codec sources plus one custom loader.
The repo is **poorly maintained and mid-modernization**. [PLAN.md](PLAN.md) is the source of truth for what is broken and what comes next.

## Read on demand (do not preload)
| Need | File |
|---|---|
| Find the right file for a task | [docs/INDEX.md](docs/INDEX.md) (task router) |
| One line per class, all modules | [docs/SOURCE_MAP.md](docs/SOURCE_MAP.md) |
| Runtime flow, threads, data model, extension points | [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) |
| Libraries, vendored code, native DLLs | [docs/DEPENDENCIES.md](docs/DEPENDENCIES.md) |
| Natives per platform: sources, bundles, loader modes, dist zips | [docs/NATIVES.md](docs/NATIVES.md) |
| Toolchain, build blockers, running | [docs/BUILD.md](docs/BUILD.md) |
| Behavior: formats, keys, config keys, files on disk | [SPEC.md](SPEC.md) |
| Goals, philosophy, non-goals | [INTENT.md](INTENT.md) |

## Modules (build order)
| Module | Role | javac level |
|---|---|---|
| `commons-j7/` | Home-grown utilities: `StringUtils`, `FileUtils`, `FilePath`, `jni.LibraryLoader`, … (dead packages removed in P2.3). | 21 |
| `thirddep-j7/` | **Vendored third-party sources, patched so their natives load through `LibraryLoader`:** SWT = stock 3.135 classes unpacked at build time + patched `Library` in `src/swt-stock` (all flavors; the vendored win32 4.527 sources were deleted in P7), 7-Zip-JBinding 16.02-2.01, TurboJPEG, Luciad WebP, vavi AVIF (JNA), JXL (JNI), mortennobel scaling. Also custom ImageIO SPIs. | 21 |
| `xanderView/` | The app. Main class: `org.delafer.xanderView.gui.MainEntryPoint` | 21 |

## Commands
Use the Maven Wrapper (Maven 3.9.16, downloaded once). Java isn't on PATH here: set `JAVA_HOME` to `devtools/jdk-26.0.2.1+1` (Temurin 26, untracked; `javac-check.sh` finds it itself) (IntelliJ's bundled Maven 3.9.16 also works; path in [BUILD.md](docs/BUILD.md)). **`./mvnw -B -DskipTests package` succeeds** with no manual setup (verified 2026-10-12); OpenCV comes from the WPILib Maven repo. BUILD.md explains what is still hard: custom natives, jitpack-only vavi-commons.
```bash
bash tools/javac-check.sh
```
```bash
./mvnw -B -DskipTests package
```
```bash
./mvnw -B -DskipTests -Dswt.flavor=gtk.linux.x86_64 package
```
```bash
java -jar xanderView/target/xanderview-j7-1.0.0.jar "D:\pics"
```
1. `tools/javac-check.sh` compiles all modules (Windows SWT flavor) with plain javac in about 30 s. Use it as the quick compile check.
2. `./mvnw -B -DskipTests package` builds everything; only the app jar is shaded. Windows builds into `target/`.
3. `-Dswt.flavor=<ws>.<os>.<arch>` (`gtk.linux.x86_64|aarch64`, `cocoa.macosx.x86_64|aarch64`) cross-builds a Linux/macOS jar into `target-<flavor>/`.
4. The last command runs the app; no JVM flags are needed (manifest `Enable-Native-Access`; no `--add-exports` since P7). The argument can be a file, a directory or an archive; without one, the app resumes from `_zlst.csv`. `--check-natives` instead of a path loads every native of the shipped manifest headless and exits.

- **Tests: none.** The repo has 0 `@Test`. The files under `*/src/test` are scratch `main()` programs; run them one at a time from the IDE. "Run a single test" does not apply until JUnit is added (PLAN P3.1).
- **Natives are not in git.** Since P7 they are upstream builds pinned by SHA-256 in `natives/<platform>.tsv`, bundled by `tools/natives/bundle.sh` and shipped as `libs/<os>/<arch>/` + `natives.sha256` next to the jar (per-platform dist zips; the build downloads the bundle when `xview.natives.skip=false`, or takes `-Dxview.natives.dir`). The loader copies each native once into `%APPDATA%\xanderView\libs\<os>\<arch>\<bundle12>\` and verifies it. The owner's deployed install `C:\Program Files\xview\libs\windows\amd64\` (no manifest) still works in the legacy exact-compare mode via `-Dxview.libs.location`/`XVIEW_LIBS_LOCATION`. Windows x64 natives: `swt`, `lib7-Zip-JBinding`, `turbojpeg`, `webp-imageio`, `libavif`, `libjxl_jni`, `opencv_java460` (OpenCV also needs `concrt140.dll` from the VC++ redistributable). `java -jar xanderview.jar --check-natives` checks them. Details: [NATIVES.md](docs/NATIVES.md), [DEPENDENCIES §4](docs/DEPENDENCIES.md#natives).
## Big picture (details: [ARCHITECTURE.md](docs/ARCHITECTURE.md))
- **UI**: `MainWindow` (SWT `Display`, `MultiShell` = a windowed shell plus a fullscreen shell) → EMBEDDED `Composite` → `SWT_AWT` frame → AWT `ImageCanvas`. All keys go through one `Display` filter into `MainWindow.bindKeyEvent`, a switch on `SWT.*` key constants and character literals.
- **Playlist**: `CommonContainer(Ext)` iterates a sorted list of `ImageAbstract<E>` entries. The entries come from an `IAbstractReader`: `FileReader` for directories (with a WatchService and optional recursion into archives) or `SevenZipReader` for zip/rar/7z/jzip.
- **Entries**: `ImageFS` (a path) and `ImageZip` (an archive index). Decorators: `ImageDec` decrypts `.cry`, `ImageEnc` encrypts on copy, `ImageAimDec` takes the first image of an `.aim`. `content()` returns a `Buf` (ByteBuffer, may be mmapped). **Close it.**
- **Identity ("CRC")**: file size plus xxHash64 of the **first 3072 bytes** (`Hasher.HSIZE`). It keys saved view state and copy dedup.
- **Render**: `ImageLoader` decodes (JPEG via TurboJPEG with DCT downscale, everything else via ImageIO with WebP/AVIF/JXL SPIs). Then `ImageCanvas.preRenderImage` runs: fit → zoom ×1.05ⁿ → `ScaleFactory` (resizer chosen by pixel count from the `Scaler` config) → `Rotator2D` → `GammaCorrection`.
- **State**: `ApplConfiguration` (`%APPDATA%\xanderView\xander.properties`), `OrientationStore` (`imagedata.bin`, a CRC→packed long), `CopyService` (one instance per target dir; indexes the target to mark images already copied).
- **Natives**: every native goes through `net.j7.commons.jni.LibraryLoader`:
  - It copies `<location>/libs/<os>/<arch>/<file>` into `<AppData>/libs/<os>/<arch>/` when the cache file is missing or **its content differs**. The check is exact, runs on the first request per JVM, and publishes atomically.
  - Then it calls `System.load` (JNI), or only sets `jna.library.path` (JNA: libavif).
  - A missing library **soft-fails**: a logged warning, then a later `UnsatisfiedLinkError` (`tryLoadLibrary` lets a caller fail fast). On Linux/macOS it also tries `lib<name>` and `.dylib`/`.jnilib`.
  - Vendored libraries are patched to call it ([ARCHITECTURE §6](docs/ARCHITECTURE.md)).
  - Manifest mode (P7): a source with `natives.sha256` gets a content-addressed cache dir per bundle, verified once, trusted afterwards; `natives.deps` siblings are loaded first; the jar carries the manifest it was built with.

## Rules
- **Keep the custom native-loading design.** Don't replace vendored native-backed libraries with stock jars that bundle and self-extract natives, and don't replace the minified OpenCV jar with the official one. Every native goes through `LibraryLoader` and the custom builds ([DEPENDENCIES §0](docs/DEPENDENCIES.md)).
- **Platforms:** Windows/Linux/macOS on x86_64 are required targets and aarch64 is optional, but only Windows x64 natives exist today (Linux/macOS jars build with the stock-SWT flavors). New code must not add *new* Windows-only assumptions; use `SysInfo` and `LibraryLoader` for OS/arch paths. Porting work is PLAN P5.
- **`thirddep-j7` is vendored code.** Do not reformat or refactor it. Keep patches minimal and tag them `// xview-patch: <why>`. Pure-Java libraries may be replaced by Maven artifacts; native-backed ones may not. See [.claude/rules/thirddep.md](.claude/rules/thirddep.md).
- **Persisted formats are contracts.** These all affect existing user data, so change them only together with a migration:
  - the `Hasher` algorithm and `HSIZE`
  - the `OrientationStore` bit layout (`Range` offsets)
  - the `.cry` layout and key derivation (`Encryptor`, `CipherKey`)
  - the `xander.properties` keys
- Singletons use the Bill-Pugh `Holder` idiom with `instance()`. Follow it.
- These are the owner's local assets. Never delete, move or commit them unless asked:
  - untracked `*.bak` files (ignored by the owner's global gitignore)
  - the binaries in `xanderView/lib/`, `xanderView/opencv/` and `thirddep-j7/src/test/resources/libs/`
  - the deployed install `C:\Program Files\xview\` and the cache `%APPDATA%\xanderView\`
- Before declaring a code change done, compile the affected module (`mvn -pl <module> -am compile`, or the javac fallback).

## Doc sync (mandatory, same change as the code)
| Code change | Update |
|---|---|
| Class added, removed, renamed or repurposed | [docs/SOURCE_MAP.md](docs/SOURCE_MAP.md) |
| Keys, formats, config keys, files on disk, user-visible behavior | [SPEC.md](SPEC.md) |
| Flow, threading, data model, extension points | [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) |
| pom, dependency, native DLL, build step | [docs/DEPENDENCIES.md](docs/DEPENDENCIES.md), [docs/BUILD.md](docs/BUILD.md) |
| PLAN item done, or a new debt found | [PLAN.md](PLAN.md) (tick it or add it) |
| New doc file | [docs/INDEX.md](docs/INDEX.md) |

Run `bash tools/check-docs.sh` afterwards. It lists classes missing from SOURCE_MAP and dead links. More detail is in [.claude/rules/docs-sync.md](.claude/rules/docs-sync.md).
