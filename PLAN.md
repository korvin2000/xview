# PLAN — modernization roadmap

> The source of truth for status. Tick items in the same change that completes them, and add newly found debt here.
> Guardrails:
> - Preserve behavior and the persisted formats ([CLAUDE.md](CLAUDE.md) → Rules).
> - **Keep the custom native-loading design**: vendored and patched libraries, `LibraryLoader`, custom DLL builds, the minified OpenCV jar ([DEPENDENCIES §0](docs/DEPENDENCIES.md)).
> - After every step, run `./mvnw -B -DskipTests package` (see [BUILD](docs/BUILD.md)) or at least `bash tools/javac-check.sh`.

## Status (2026-10-12, after the second fix round)
- **Build: `mvnw -B -DskipTests package` → BUILD SUCCESS on a clean `~/.m2`-independent setup:** Maven Wrapper (3.9.16, SHA-256 pinned), enforcer (JDK ≥ 21, Maven ≥ 3.9), OpenCV from the WPILib repo (byte-identical jar), `release=21` everywhere, plugins pinned, only the app shades. `tools/javac-check.sh` passes.
- **SWT flavors:** Windows builds with the vendored SWT 4.527 (unchanged). `-Dswt.flavor=gtk.linux.x86_64|gtk.linux.aarch64|cocoa.macosx.x86_64|cocoa.macosx.aarch64` cross-builds the Linux/macOS jars on any machine (verified on Windows for gtk x86_64 and cocoa aarch64: the app compiles against SWT 3.135, the jars hold no SWT natives). Linux/macOS **runtime is unverified**: their natives don't exist yet (P5.3).
- Runtime on Windows is verified by the headless native probe (isolated `%APPDATA%`, all 8 natives from a cold cache) and a short GUI start with `java -jar` and **no** JVM flags (manifest `Add-Exports`). The owner's deployed install `C:\Program Files\xview` and the cache `%APPDATA%\xanderView\libs\windows\amd64\` were not modified.
- Natives aren't in git (P1.9 open). There are no tests (P3.1 deferred). Dead code was removed (P2).
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
- [x] P1.11 SWT was in the build twice. The vendored **4.527** (which matches the custom `swt.dll`, whose own version string is `swt-win32-4527`) is kept, and the `org.eclipse.swt…:4.3` artifact is removed. Its 38 `SWTMessages*.properties` were extracted (now in `thirddep-j7/src/swt-win32/resources/org/eclipse/swt/internal/`).
- [x] P1.12 `ServicesResourceTransformer` in the app shade config (the only shade since P1.7), so `META-INF/services` files are merged instead of first-wins. Verified: 3 readers (AVIF, WebP, JXL-JNI) and 2 writers.
- [x] P1.4 OpenCV: the small classes-only jar is resolved as `edu.wpi.first.thirdparty.frc2023.opencv:opencv-java:4.6.0-5` from `https://frcmaven.wpi.edu/artifactory/release/` (repository `wpilib` in the parent pom, `checksumPolicy=fail`). It is **byte-identical** to the former local install (SHA-256 `dd74cfd4…697b6`, verified). No manual install step; `register.cmd` is obsolete.
- [x] P1.5 Maven Wrapper 3.3.4 (`only-script`; `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`: Maven 3.9.16 + `distributionSha256Sum`). `maven-enforcer` 3.6.3 in the parent: JDK ≥ 21, Maven ≥ 3.9, and an SWT flavor must be selected. Unix `mvnw` needs `unzip` (without it, it fetches the `.tar.gz`, which fails the zip checksum). `.gitattributes` keeps `mvnw` LF.
- [x] P1.6 Parent: `maven.compiler.release=21` for all modules (commons/thirddep were 16); the compile-time `--add-exports`/`--add-opens` are gone (not needed); all plugin versions pinned in `pluginManagement`. App manifest: `Add-Exports: java.desktop/sun.awt.windows` and `Enable-Native-Access: ALL-UNNAMED`, so `java -jar` needs no flags (verified on Windows).
- [x] P1.7 Shade removed from `commons-j7` and `thirddep-j7` (with it the bogus `Main-Class`); only the app shades. The overlap warnings are gone except the benign `META-INF/MANIFEST.MF`.
- [x] P1.8 Logging: logback removed everywhere (including an unused import in `commons/utils/DateUtils`); `slf4j-simple` 1.7.10 is the only binding and lives in the app only (`commons-j7` has just `slf4j-api`). Config: `xanderView/src/main/resources/simplelogger.properties`.
- [ ] P1.9 Natives source, reproducible **within the LibraryLoader design**:
  - [x] `location` is overridable: `-Dxview.libs.location=<dir or URL>` > env `XVIEW_LIBS_LOCATION` > `libs.properties` `location` > the jar that holds `libs.properties` (`LibraryLoader.getLocations`).
  - [ ] Remove the hard-coded `C:/Program Files/xview` from `libs.properties` (owner decision: default to the jar's directory?).
  - [ ] Version the native tree `libs/<os>/<arch>/` outside the jar (owner decision: Git LFS in-repo, or a release archive plus a SHA-256 manifest).
  - [ ] Document how each custom DLL is built (compiler, flags, upstream version) — needs the owner's build notes.
- [x] P1.10 `.gitignore`: `**/target/`, `**/target-*/` (cross-flavor builds), `**/dependency-reduced-pom.xml`, `*.bak`, `*.javabak`, natives (`*.dll`, `*.so`, `*.dylib`, `*.jnilib`, `*.lib`); `commons-j7/target/**` untracked (files kept on disk).

## P2 — Remove trash and dead code ✅
- [x] P2.1 Tracked trash deleted: `commons-j7/target/**` (untracked), the 8 `*.java.txt`, `howtoaddrepos.txt`, `RandomComparatorOld.javabak`, `thirddep-j7/src/main/java/{chrome.manifest,swt.js,swt.xpt,version.txt}`, `imageViewer.cmd`.
- [x] P2.2 App dead code deleted (owner decided: no benchmark code kept): `scale/deprecated/*`, `scale/others/*`, `ResizerJAI`, `ResizerOpenCV`, `filter/*`, `FilePointer`, `Bytes`, `Processor`, `Transliterator`, `NameIncrementer`, `SimpleNameIncrementer`, `RotatorCPU`/`RotatorGPU`, `no.nixx.opencl`, the `.cl` kernels, the scratch tests that only used them (`scale/deprecated/*`, `scale/test/{TestDownscale,Tester,TesterQuality,TestOpenCvResizer,TestSpeed,TestUpscale,TestUpscale2}`), and the then-unused deps `jocl` and `thumbnailator`.
- [x] P2.3 commons-j7 dead packages deleted: `logger`, `net`, `reflection` (+ its scratch test), `ui`, `xml`, `io.zip`, the `org.apache.commons.io` and `org.eclipse.osgi` copies; deps `commons-cli`, `jaxb-api` and the tmatesoft repo removed. **Kept** `io.helpers.WildcardMatcher` (used by `FileUtils` and `AbstractFileProcessor`), `jni`, `io.SysInfo`, `io.FilePath`.
- [x] P2.4 thirddep:
  - The disabled JXL-JNA path is deleted: `vavi.imageio.jpegxl.jna`, `vavi.awt.image.jna.jpegxl`, and the JNA facade `vavi.image.jpegxl.JpegXL` (it only served that path), plus the commented SPI line. **JPEG XL still works** through the JNI reader `vavi.imageio.jpegxl.jni` + `org.jpeg.jpegxl.wrapper` → `libjxl_jni.dll`.
  - The owner's untracked JNA DLLs in `thirddep-j7/src/main/resources/win32-x86-64/` (packed into the jar until now; JNA would have self-extracted them) were **deleted** (owner decision); copies of `jxl`, `jxl_jni`, `jxl_threads` remain in `xanderView/lib/jxl/`.
  - SWT `browser`, `internal.mozilla`, `internal.webkit`, `opengl`, `internal.opengl.win32` and the Mozilla resources (`chrome.manifest`, `swt.js`, `swt.xpt`) deleted. `ole.win32` is **kept**: `accessibility/Accessible` uses `OLE`.
- [x] P2.5 `Geometry` reduced to the 2 methods in use (`distanceSquared`, `centerPoint`).

## P3 — Safety net and code quality
- [ ] P3.1 (TODO, deferred) JUnit 5 + surefire in the parent. First tests should guard the contracts:
  - the `OrientationStore` bit packing round-trip
  - the `Encryptor` `.cry` round-trip plus a golden file
  - `Hasher` stability (golden value)
  - `ScalerHelper` parsing
  - `CommonContainer` navigation (direction, loop, ±10) and concurrent watcher events (P3.5)
  - `DefaultNameIncrementer`
  - `LibraryLoader` path computation per OS/arch and the file-name candidates (P5.2), incl. a stale cache candidate vs a new source candidate
- [x] P3.2 `MainWindow.bindKeyEvent` uses `SWT.*` constants and char literals (identical values); the debug `println`s with wrong F-key labels are gone; unbound keys log at debug level.
- [ ] P3.3 (TODO, deferred) Move decode/scale off the SWT UI thread (today a large image blocks input; see [ARCHITECTURE §3](docs/ARCHITECTURE.md)).
- [x] P3.4 `System.out`/`System.err`/`printStackTrace` → SLF4J in the app and commons main code (vendored thirddep and scratch `main()` methods unchanged).
- [x] P3.5 Watcher callbacks vs the UI: `CommonContainer`'s list, cursor and current entry are guarded by the container monitor; the watcher does its IO outside the lock; the cursor setup in the constructor is atomic; duplicate creates are skipped; `CommonContainerExt` never holds the monitor across splashes or copy-index checks; the caption reads index and size atomically (`position()`). `FileReader` registers the watch **before** the scan (no lost creations), survives a vanished file, ignores events after `close()`, and falls back to a top-level watch where `FILE_TREE` is unsupported. `CopyService`'s debug traversal now holds the set's monitor.

## P4 — Dependency modernization (one library per step; keep custom native loading)
- [ ] P4.1 Pure-Java libraries can be swapped freely: mortennobel → `com.mortennobel:java-image-scaling:0.8.6`; lz4 → `org.lz4:lz4-java`; hppc 0.10; slf4j 2; unidecode. (jocl and thumbnailator are already gone, P2.2.)
- [ ] P4.2 Native-backed libraries: upgrade only via hooks that keep `LibraryLoader` and the custom builds. Status (2026-10-12):
  - [x] 7-Zip-JBinding: the vendored Java is **already 16.02-2.01** (`SevenZip.SEVENZIPJBINDING_VERSION`), the latest upstream release, and it matches the DLL's 27 JNI exports. Nothing to upgrade (the former "9.20-era" note was wrong).
  - [ ] TurboJPEG: `turbojpeg.dll` is libjpeg-turbo **3.0.91** (3.1 beta, build 20240924); the vendored Java's native methods match its JNI exports 1:1. Moving to the 3.1.x final Java API needs a rebuilt `turbojpeg.dll` first. Done meanwhile: `TJLoader` calls `LibraryLoader` directly instead of SWT's `Library` (needed by P5.1).
  - [ ] SWT win32: a newer SWT needs a matching `swt-win32-<ver>.dll`. Only the custom 4527 build exists, so it stays at 4.527. Option: drop in the official DLLs from the Eclipse jar (loaded through `LibraryLoader`), then switch the win32 flavor to the stock-SWT overlay like Linux/macOS.
  - WebP: keep it (owner decision).
  - Each upgrade needs a rebuilt matching DLL.
- [ ] P4.3 vavi AVIF/JXL: keep vendored, and mark the existing patches `// xview-patch`. Upstream vavi-image-avif 0.0.7 and vavi-image-jpegxl 0.0.7 are jitpack-only; jpegxl depends on the failed vavi-image 1.0.13 build. (Owner: keep untouched for now.)

## P5 — Cross-platform: Windows, Linux, macOS on x86_64 (required) and aarch64 (optional)
- [x] P5.1 SWT per OS by Maven profiles in the parent pom (`swt-*`, auto-selected by OS/arch, or forced with `-Dswt.flavor=<ws>.<os>.<arch>`):
  - win32: the vendored SWT 4.527 sources, moved unchanged to `thirddep-j7/src/swt-win32/`.
  - gtk/cocoa: the stock `org.eclipse.platform:org.eclipse.swt.<ws>.<os>.<arch>:3.135.0` jar is **unpacked at build time** into thirddep without its natives, signatures and stock `Library`; the patched `thirddep-j7/src/swt-stock/java/org/eclipse/swt/internal/Library.java` loads every SWT native through `LibraryLoader` (no extraction, no `~/.swt`). Load names stay versioned (`swt-pi3-gtk-4974r10` → `libswt-pi3-gtk-4974r10.so`).
  - Non-Windows builds go to `target-<flavor>/`, so flavors never mix in one output dir. Manifests carry the flavor's `SWT-OS/WS/Arch`.
  - The app no longer uses SWT internals (`MultiShell` dropped `internal.win32.OS`).
  - Owner note: this uses stock SWT **Java classes** for Linux/macOS; natives and loading stay custom.
- [x] P5.2 `LibraryLoader` for non-Windows: file-name candidates per load name (Unix: `<name>` then `lib<name>`; macOS: `.dylib` then `.jnilib`, JNA only `.dylib`); a candidate in the source beats one only in the cache; Windows unchanged (single candidate). `arm64` → `aarch64`. B6 order bug fixed. Verified by a scratch probe with faked `os.name`/`os.arch` (no JUnit yet, P3.1).
- [ ] P5.3 Build the custom natives per platform (swt, 7-Zip-JBinding, turbojpeg, webp-imageio, libavif + runtime, libjxl_jni, opencv_java460) into `libs/<linux|macos>/<x86_64|aarch64>/`. Names: see [DEPENDENCIES §4](docs/DEPENDENCIES.md#natives). Sibling dependencies need static linking or `$ORIGIN`/`@loader_path` rpaths (`System.load` from the cache does not add the directory to the linker path).
- [x] P5.4 Per-OS runtime flags:
  - Windows: manifest `Add-Exports: java.desktop/sun.awt.windows` (+ `Enable-Native-Access`).
  - macOS: `PlatformLaunch` relaunches the app once with `-XstartOnFirstThread` when `JAVA_STARTED_ON_FIRST_THREAD_<pid>` is missing.
  - Linux: under Wayland (with XWayland) it relaunches with `GDK_BACKEND=x11`, because `SWT_AWT` embeds via XEmbed; a user-set `GDK_BACKEND` is respected.
  - SWT 3.135 gtk/cocoa create the embedded AWT frame through JNI, so they need no `--add-exports`. Unverified at runtime (P5.3). macOS limits: AWT is always Retina-scaled (ignores `sun.java2d.uiScale`), `SWT_AWT.new_Shell` is unsupported (unused), never `invokeAndWait` from the SWT thread. Pure SWT painting stays a fallback idea if embedding fails.
- [ ] P5.5 Windows-only code paths: `ImageRepository` shell icons, `A:\` defaults, the drive-letter stripping in `_zlst.csv` handling, `FILE_TREE` recursive watching (B14; non-Windows now watches only the top directory).

## Known issues / bugs (verify before fixing; each needs an owner decision)
Status: ✅ fixed and verified · 🟡 mitigated / not reproducible · open otherwise. F-numbers refer to [ADDITIONAL_INFO §5](docs/ADDITIONAL_INFO.md).
| # | Where | Observation | Status |
|---|---|---|---|
| B1 | `ApplConfiguration.defaults` `Scaler` + sample `xander.properties` | The typo `…5,2500000;6;6000000;…` made 2.5–6 MP images use CV_LINEAR (4) | ✅ now `6,6000000`. Verified: only (2,500,000, 6,000,000] px changes, from 4 to 6. Existing user config files are untouched. |
| B2 | `ApplConfiguration.setCopyDir` | Only updates the in-memory `dirs` array; `target.folder` is never rewritten, so the Ctrl+D choice is likely lost on restart | open |
| B3 | `ScaleFactory.getInstanceByType0` | An unknown scaler id gives `switch` on a null enum → NPE | open |
| B4 | `CommonContainer` last entry | The drive letter is stripped when *reading* `_zlst.csv`. Commas in file names are ambiguous (F7). | open |
| B5 | `FileReader`/`CopyService` watchers | Mutate the lists without synchronization while the UI thread iterates them | ✅ for the playlist (P3.5). `CopyService` uses a synchronized set; its compound replace in `addImage` (`exists` → `remove` → `add`) is still not atomic (see B11). |
| B6 | `commons-j7/.../LibraryLoader` | Scratch `main()` with a `W:\avif` path; hard-coded app name `xanderView`; `getArch()` reads `model` before it is assigned, so the 64→32 mapping never applies | ✅ `main()` removed and the order fixed (no effect on 64-bit JVMs). The app name stays a deployment contract. |
| B7 | `commons-j7/.../io/SysInfo.getAppDataDir` | The loop started at 0 (`default` = candidate 1), so `APPDATA` was tried twice and `java.io.tmpdir` never | ✅ loop is now 1..size(). Verified: the normal path is unchanged; all-unwritable now gives tmpdir instead of `null`. |
| B8 | `LibraryLoader` | The cache was never refreshed, and copies went straight to the final name | ✅ exact content comparison on first use per JVM; atomic temp publish; rename-old fallback for a mapped DLL; load from the source if the cache can't be replaced; an unreadable source keeps the cache; rollback never overwrites another instance's copy; one reload retry if the file vanishes mid-swap; stale-leftover sweep; COPIED→LOADED promotion. Verified by the probe (cold cache, stale same-size file, locked DLL, locked source). Residual risk: there is no cross-process lock. |
| B9 | `Avif` / `SevenZip` | `libgcc_s_seh-1.dll` was copied only by 7-Zip init | 🟡 **Not reproducible:** the old jar decodes AVIF on a fresh cache without it (libavif 1.1.1). As a preventive step, `LibraryLoader` copies it next to `libavif` on Windows (copy only, never loaded). |
| B10 | Missing natives | `LibraryLoader` soft-fails (a warning), and the error appears later as `UnsatisfiedLinkError`. There is no startup check of the 8 required DLLs. | open (the stock-SWT `Library` overlay fails fast via `tryLoadLibrary`) |
| B11 | `CopyService.copySync` (F1) | Queued copies deduplicate through the *current global* target (`exists(entry)` → `instance()`), not the job's own target. `CopyObserver` can mark the wrong image. | open (high) |
| B12 | `CopyService` worker, `Shutdown` (F2) | The worker dies on an observer exception and the queue strands. Shutdown waits without a bound and flushes history before the queue drains. `checkInitialized` can wait forever. | open (high) |
| B13 | `CopyService` + `FileReader` (F3) | Target indexing applies `filterCount` sampling and a `FILE_TREE` watch to a flat scan, so the dedup index can be incomplete | open (high) |
| B14 | `FileReader` watcher (F4) | Ignores `OVERFLOW` and modifications. `ExtendedWatchEventModifier.FILE_TREE` is Windows-only. | 🟡 non-Windows falls back to a top-level watch instead of failing (P3.5); `OVERFLOW` still needs a rescan; recursion on Linux/macOS is P5.5 |
| B15 | `SevenZipReader`, `ImageZip` (F5) | A second stream isn't closed. `ExtractOperationResult` is ignored. long→int size narrowing; no decompression budget. | open |
| B16 | `ApplConfiguration.save`, `OrientationStore.save` (F6) | The live properties file is truncated in place; `unsaved`/`dirty` are cleared even when the save fails. No multi-instance locking. | open |
| B17 | `FileReader.getContainerPath` (F7) | A regular-file argument yields an empty container path (the watcher then watches the working directory) | open (verify) |
| B18 | `gui/ImageLoader.loadJpegImage` | Catches `Exception` only, so a missing or incompatible `turbojpeg` (`UnsatisfiedLinkError`) bypasses the ImageIO fallback | open |
| B19 | `vavi` AVIF writer + custom `libavif.dll` | The DLL is decode-only ("No codec available" when encoding). The writer SPI is still registered. | open (document or unregister) |
| B20 | `MainWindow` path switch (`pointer.close()`), `ImageZip.content` | Switching the source closes the reader while a queued decode (AWT EDT) or copy can still use its entries (e.g. an archive handle). Found in the P3.5 review. | open: coordinate reader lifetime with outstanding work, drop stale renders |
| B21 | parent `swt-*` profiles, `thirddep-j7/pom.xml`, `xanderView/pom.xml` | All SWT flavors share the Maven coordinates of thirddep and the app; a module-only build (`-pl xanderView` without `-am`) of another flavor would shade the thirddep from `~/.m2`. Found in the Codex diff review. | 🟡 non-Windows flavors skip install/deploy (only the Windows flavor reaches `~/.m2`), documented in BUILD; full fix: a flavor classifier for thirddep and the app |
