# PLAN — modernization roadmap

> The source of truth for status. Tick items in the same change that completes them, and add newly found debt here.
> Guardrails:
> - Preserve behavior and the persisted formats ([CLAUDE.md](CLAUDE.md) → Rules).
> - **Keep the custom native-loading design**: vendored and patched libraries, `LibraryLoader`, custom DLL builds, the minified OpenCV jar ([DEPENDENCIES §0](docs/DEPENDENCIES.md)).
> > - After every step, run `mvn -B -DskipTests package` (see [BUILD](docs/BUILD.md)) or at least `bash tools/javac-check.sh`.

## Status (2026-10-12, after the first fix round)
- **Maven build: BUILD SUCCESS.** This needs the one-time local OpenCV install (P1.4 is still open for reproducibility). `tools/javac-check.sh` passes with **no** workarounds.
- Runtime is verified by a headless probe with an isolated `%APPDATA%`. All 8 natives load from a cold cache: WebP round-trip, TurboJPEG decode, OpenCV resize, 7-Zip, plus AVIF and JXL decode of real files. A GUI launch was not performed.
- The owner's deployed install `C:\Program Files\xview` (jar, `libs\`, JRE 23) and the cache `%APPDATA%\xanderView\libs\windows\amd64\` were not modified. The deployed jar is the old build.
- Natives aren't in git. There are no tests (the probe is a scratch tool, not committed). About 30% of the app's classes and about 70% of commons-j7 are dead (estimate).
- Independent review: [docs/ADDITIONAL_INFO.md](docs/ADDITIONAL_INFO.md). Its findings F1–F7 are tracked as B11–B17 below.

## P0 — Documentation and agent setup ✅
- [x] CLAUDE.md, INTENT, SPEC, PLAN, docs/{INDEX, SOURCE_MAP, ARCHITECTURE, DEPENDENCIES, BUILD}
- [x] `tools/javac-check.sh` (compile without Maven) and `tools/check-docs.sh` (doc drift)
- [x] `.claude/rules/` path-scoped rules; auto-memory seeded
- [x] Native-loading design, the 8-DLL inventory, the platform matrix and the vavi artifact research documented

## P1 — Reproducible build (highest value; small diffs)
- [x] P1.1 Delete the unused `net.sourceforge.jiu` imports (`ApplConfiguration`, `ScaleFactory`) and the unused imports `org.eclipse.swt.internal.win32.SIZE` and `scale.deprecated.ResizeOpenCL`.
- [x] P1.2 vavi-commons coordinates are now `com.github.umjammer.vavi-commons:vavi-commons:1.1.14` (jitpack).
- [x] P1.3 Vendored `vavi.imageio.WrappedImageInputStream` (vavi-image 1.0.11 verbatim, plus an `// xview-patch` that keeps `available()` ≥ 0) into `thirddep-j7`; the `vavi-image` dependency is removed.
- [x] P1.11 SWT was in the build twice. The vendored **4.527** (which matches the custom `swt.dll`, whose own version string is `swt-win32-4527`) is kept, and the `org.eclipse.swt…:4.3` artifact is removed from the thirddep pom and the parent `dependencyManagement`/property. Its 38 `SWTMessages*.properties` were extracted into `thirddep-j7/src/main/resources/org/eclipse/swt/internal/` so message lookups keep working. `SWT-OS`/`SWT-WS`/`SWT-Arch` were added to the app's shaded manifest.
- [x] P1.12 `ServicesResourceTransformer` in both shade configs, so `META-INF/services` files are merged instead of first-wins. Verified: 3 readers (the JNA-JXL line is still commented out) and 2 writers.
- [ ] P1.4 OpenCV: keep the small classes-only jar (WPILib `opencv-java` 4.6.0-5, SHA-256 `dd74cfd4…697b6`).
  - Done: the one-time local install, the same as `register.cmd`.
  - Open: a reproducible source. Options are a project-local file repository (`repo/org/opencv/opencv/4.6.0/…` + `<repository><url>file://${maven.multiModuleProjectDirectory}/repo</url>`) or a documented install step in mvnw, with a checksum check.
- [ ] P1.5 Add the Maven Wrapper (`mvnw`, Maven 3.9.x) and `maven-enforcer` (JDK ≥ 21).
- [ ] P1.6 Unify compiler config in the parent:
  - Drop the compile-time `--add-exports`, then set `maven.compiler.release=21` for all modules.
  - Add the manifest entry `Add-Exports: java.desktop/sun.awt.windows` to the shaded app jar, so `java -jar` works without flags.
  - Pin plugin versions in `pluginManagement`.
- [ ] P1.7 Fix commons-j7 shade: remove the bogus `Main-Class`, or drop shade for the library modules. Nested shading makes Maven warn about hundreds of overlapping classes: commons↔slf4j/jaxb, thirddep↔jna/vavi-commons.
- [ ] P1.8 Logging: keep one SLF4J binding (remove `slf4j-simple`, or remove logback).
- [ ] P1.9 Natives source, reproducible **within the LibraryLoader design**:
  - Version the native tree `libs/<os>/<arch>/` outside the jar: Git LFS in-repo, or a release archive plus a checksum manifest.
  - Make `location` configurable: system property or env var with a fallback to the jar's directory, instead of the hard-coded `C:/Program Files/xview` in `libs.properties`.
  - Document how each custom DLL is built (compiler, flags, upstream version).
- [ ] P1.10 `.gitignore`: add `**/target/`, `**/dependency-reduced-pom.xml`, `*.bak`, `*.javabak`, natives; untrack `commons-j7/target/**`.

## P2 — Remove trash and dead code (one package per commit; grep before deleting)
- [ ] P2.1 Tracked trash:
  - `commons-j7/target/**` (4 files)
  - `commons-j7/src/test/java/net/j7/commons/others/*.java.txt` (8)
  - `commons-j7/howtoaddrepos.txt`
  - `xanderView/.../comparator/RandomComparatorOld.javabak`
  - `thirddep-j7/src/main/java/{chrome.manifest,swt.js,swt.xpt,version.txt}`
  - `xanderView/src/test/resources/imageViewer.cmd` (obsolete flags)
- [ ] P2.2 App dead code ([SOURCE_MAP](docs/SOURCE_MAP.md) status `D`/`X`/`T`):
  - `scale/deprecated/*` (~2.7k LOC, including the imgscalr copy), `scale/others/*`, `ResizerJAI`, `ResizerOpenCV` (tests only)
  - `filter/*`, `FilePointer`, `Bytes`, `Processor`, `Transliterator`
  - `NameIncrementer`, `SimpleNameIncrementer`, `RotatorCPU`/`RotatorGPU` (+ `no.nixx.opencl`, `jocl`, the `.cl` kernels)
  - Ask the owner first: INTENT has an open question about benchmark code.
- [ ] P2.3 commons-j7 dead packages: `logger`, `net`, `reflection`, `ui`, `xml` (+ jaxb-api), `io.zip`, `io.helpers`, the `org.apache.commons.io` and `org.eclipse.osgi` copies, the unused deps (`commons-cli`) and the tmatesoft repo. **Keep** `jni`, `io.SysInfo` and `io.FilePath` (native loading).
- [ ] P2.4 thirddep:
  - Delete the disabled JXL-JNA path (`vavi.imageio.jpegxl.jna`, `vavi.awt.image.jna.jpegxl`) and its `jxl*/brotli*` DLLs in `src/main/resources/win32-x86-64/`, after confirming they are unused.
  - Delete the SWT browser/mozilla/webkit/ole/opengl packages if nothing references them.
- [ ] P2.5 `Geometry` (797 LOC JFace copy) → keep only the 2 methods in use.

## P3 — Safety net and code quality
- [ ] P3.1 JUnit 5 + surefire in the parent. First tests should guard the contracts:
  - the `OrientationStore` bit packing round-trip
  - the `Encryptor` `.cry` round-trip plus a golden file
  - `Hasher` stability (golden value)
  - `ScalerHelper` parsing
  - `CommonContainer` navigation (direction, loop, ±10)
  - `DefaultNameIncrementer`
  - `LibraryLoader` path computation per OS/arch
- [ ] P3.2 Replace the raw key codes in `MainWindow.bindKeyEvent` with `SWT.*` constants. Remove the debug `println`s (some have wrong labels: "F3" is printed for F7–F9).
- [ ] P3.3 Move decode/scale off the SWT UI thread (today a large image blocks input; see [ARCHITECTURE §3](docs/ARCHITECTURE.md)).
- [ ] P3.4 Replace `System.out`/`printStackTrace` with SLF4J.
- [ ] P3.5 Make reader watcher callbacks thread-safe against the UI iterating `SortedLinkedList`.

## P4 — Dependency modernization (one library per step; keep custom native loading)
- [ ] P4.1 Pure-Java libraries can be swapped freely: mortennobel → `com.mortennobel:java-image-scaling:0.8.6`; lz4 → `org.lz4:lz4-java`; hppc 0.10; slf4j 2 / logback 1.5; unidecode. Remove jhlabs/thumbnailator/jocl if they are unused after P2.
- [ ] P4.2 Native-backed libraries: upgrade only via hooks that keep `LibraryLoader` and the custom builds. Per library:
  - 7-Zip-JBinding 16.02: core jar + manual load + `initLoadedLibraries()`
  - SWT: newer win32 sources with the `Library` patch re-applied, then drop the conflicting `4.3` artifact
  - TurboJPEG: the 3.x Java API with the loader patch
  - WebP: keep it, or evaluate pure-Java `vavi-image-webp` 0.2.6 for decode (check VP8L, alpha and speed)
  - Each needs a rebuilt matching DLL.
- [ ] P4.3 vavi AVIF/JXL: keep vendored, and mark the existing patches `// xview-patch`. Upstream vavi-image-avif 0.0.7 and vavi-image-jpegxl 0.0.7 are jitpack-only; jpegxl depends on the failed vavi-image 1.0.13 build.

## P5 — Cross-platform: Windows, Linux, macOS on x86_64 (required) and aarch64 (optional)
- [ ] P5.1 SWT Java code is the **win32 flavor**. Introduce per-OS SWT source sets or artifacts (gtk for Linux, cocoa for macOS) selected by Maven profiles, keeping the `Library` → `LibraryLoader` patch.
- [ ] P5.2 Fix `LibraryLoader` for non-Windows:
  - `.dylib` for JNA (and modern JNI) on macOS instead of `.jnilib`
  - consistent `lib` prefixes, e.g. TJLoader `turbojpeg` vs `libturbojpeg.so`
  - an `aarch64` dir mapping
  - unit tests (P3.1)
- [ ] P5.3 Build the custom natives per platform (swt, 7-Zip-JBinding, turbojpeg, webp-imageio, libavif + runtime, libjxl_jni, opencv_java460) into `libs/<linux|macos>/<x86_64|aarch64>/`.
- [ ] P5.4 Per-OS runtime flags: `sun.awt.windows` (Windows), `sun.lwawt.macosx` (macOS), X11 (Linux). Note the `SWT_AWT` limits on macOS (`-XstartOnFirstThread` vs AWT); evaluate pure SWT painting if embedding fails.
- [ ] P5.5 Windows-only code paths: `ImageRepository` shell icons, `internal.win32.OS`/`SIZE` usage, `A:\` defaults, the drive-letter stripping in `_zlst.csv` handling.

## Known issues / bugs (verify before fixing; each needs an owner decision)
Status: ✅ fixed and verified · 🟡 mitigated / not reproducible · open otherwise. F-numbers refer to [ADDITIONAL_INFO §5](docs/ADDITIONAL_INFO.md).
| # | Where | Observation | Status |
|---|---|---|---|
| B1 | `ApplConfiguration.defaults` `Scaler` + sample `xander.properties` | The typo `…5,2500000;6;6000000;…` made 2.5–6 MP images use CV_LINEAR (4) | ✅ now `6,6000000`. Verified: only (2,500,000, 6,000,000] px changes, from 4 to 6. Existing user config files are untouched. |
| B2 | `ApplConfiguration.setCopyDir` | Only updates the in-memory `dirs` array; `target.folder` is never rewritten, so the Ctrl+D choice is likely lost on restart | open |
| B3 | `ScaleFactory.getInstanceByType0` | An unknown scaler id gives `switch` on a null enum → NPE | open |
| B4 | `CommonContainer` last entry | The drive letter is stripped when *reading* `_zlst.csv`. Commas in file names are ambiguous (F7). | open |
| B5 | `FileReader`/`CopyService` watchers | Mutate the lists without synchronization while the UI thread iterates them | open |
| B6 | `commons-j7/.../LibraryLoader` | Scratch `main()` with a `W:\avif` path; hard-coded app name `xanderView`; `getArch()` reads `model` before it is assigned, so the 64→32 mapping never applies | open |
| B7 | `commons-j7/.../io/SysInfo.getAppDataDir` | The loop started at 0 (`default` = candidate 1), so `APPDATA` was tried twice and `java.io.tmpdir` never | ✅ loop is now 1..size(). Verified: the normal path is unchanged; all-unwritable now gives tmpdir instead of `null`. |
| B8 | `LibraryLoader` | The cache was never refreshed, and copies went straight to the final name | ✅ exact content comparison on first use per JVM; atomic temp publish; rename-old fallback for a mapped DLL; load from the source if the cache can't be replaced; an unreadable source keeps the cache; rollback never overwrites another instance's copy; one reload retry if the file vanishes mid-swap; stale-leftover sweep; COPIED→LOADED promotion. Verified by the probe (cold cache, stale same-size file, locked DLL, locked source). Residual risk: there is no cross-process lock. |
| B9 | `Avif` / `SevenZip` | `libgcc_s_seh-1.dll` was copied only by 7-Zip init | 🟡 **Not reproducible:** the old jar decodes AVIF on a fresh cache without it (libavif 1.1.1). As a preventive step, `LibraryLoader` copies it next to `libavif` on Windows (copy only, never loaded). |
| B10 | Missing natives | `LibraryLoader` soft-fails (stderr only), and the error appears later as `UnsatisfiedLinkError`. There is no startup check of the 8 required DLLs. | open |
| B11 | `CopyService.copySync` (F1) | Queued copies deduplicate through the *current global* target (`exists(entry)` → `instance()`), not the job's own target. `CopyObserver` can mark the wrong image. | open (high) |
| B12 | `CopyService` worker, `Shutdown` (F2) | The worker dies on an observer exception and the queue strands. Shutdown waits without a bound and flushes history before the queue drains. `checkInitialized` can wait forever. | open (high) |
| B13 | `CopyService` + `FileReader` (F3) | Target indexing applies `filterCount` sampling and a `FILE_TREE` watch to a flat scan, so the dedup index can be incomplete | open (high) |
| B14 | `FileReader` watcher (F4) | Ignores `OVERFLOW` and modifications. `ExtendedWatchEventModifier.FILE_TREE` is Windows-only. | open |
| B15 | `SevenZipReader`, `ImageZip` (F5) | A second stream isn't closed. `ExtractOperationResult` is ignored. long→int size narrowing; no decompression budget. | open |
| B16 | `ApplConfiguration.save`, `OrientationStore.save` (F6) | The live properties file is truncated in place; `unsaved`/`dirty` are cleared even when the save fails. No multi-instance locking. | open |
| B17 | `FileReader.getContainerPath` (F7) | A regular-file argument yields an empty container path | open (verify) |
| B18 | `gui/ImageLoader.loadJpegImage` | Catches `Exception` only, so a missing or incompatible `turbojpeg` (`UnsatisfiedLinkError`) bypasses the ImageIO fallback | open |
| B19 | `vavi` AVIF writer + custom `libavif.dll` | The DLL is decode-only ("No codec available" when encoding). The writer SPI is still registered. | open (document or unregister) |
