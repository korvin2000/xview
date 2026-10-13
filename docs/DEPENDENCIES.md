# Dependencies, vendored code, natives

> Verified 2026-10-12 against the poms, the source, `~/.m2`, the jitpack/Central/WPILib HTTP status, the deployed install `C:\Program Files\xview\` and the native cache `%APPDATA%\xanderView\libs\`. Versions come from the parent `pom.xml` `<properties>` unless noted.

## 0. Design constraint (read before changing any dependency)
xanderView **intentionally** vendors and patches SWT, 7-Zip-JBinding, TurboJPEG, WebP and the AVIF/JXL bindings. The goal is that **all natives go through one custom loader** (`net.j7.commons.jni.LibraryLoader`), which copies **custom, performance-optimized builds** (some built and copied by hand) once into a persistent per-user directory and loads them from there. Stock jars that bundle and self-extract natives for every OS contradict this design. The minified OpenCV jar exists for the same reason. Since PLAN P7 the shipped natives are pinned by hash in `natives/<platform>.tsv`, tracked in `natives/<os>/<arch>/` and packaged per platform ([NATIVES.md](NATIVES.md)): upstream builds where they exist, the owner's own AVX2-optimized builds on Windows x64 (`libavif`, `libjxl_jni`, `opencv_java460`) and the optimized builds of the owner's forks (7-Zip-JBinding, libjpeg-turbo, libwebp/webp4j) where their CI produces them. Mechanism and paths: [ARCHITECTURE §6](ARCHITECTURE.md). Every replacement option below must keep this loading model.
All SWT flavors (Windows since P7) use the **Java classes** of the stock SWT 3.135 jar, unpacked at build time without its natives and its loader; the natives come from the platform bundle through `LibraryLoader` (§2, PLAN P5.1/P7).

## 1. Maven dependencies
| Artifact | Version | Declared in (scope) | Actually used by | Notes |
|---|---|---|---|---|
| ~~`org.eclipse.swt:org.eclipse.swt.win32.win32.x86_64`~~ | ~~4.3~~ | **removed** (PLAN P1.11) | — | It duplicated the vendored SWT **4.527** sources. |
| `org.eclipse.platform:org.eclipse.swt.<ws>.<os>.<arch>` | 3.135.0 (`swt.stock.version`) | thirddep, **build-time unpack only** (`maven-dependency-plugin:unpack`, all `swt-*` profiles; Windows since P7) | SWT Java classes | The shipped classes come from the unpack (natives, `META-INF` signatures and the stock `org.eclipse.swt.internal.Library` excluded; the patched `Library` from `thirddep-j7/src/swt-stock/`; `overWriteReleases` so an IDE build that wiped the classes cannot leave the jar without them). Since 2026-10-13 the jar is also declared as a dependency **for the IDE only** (`provided` in thirddep, `optional` in the app, `org.eclipse.platform:*` excluded from the shade), so IntelliJ resolves `Platform` and runs without a Maven build; the `org.eclipse.swt` parent artifact is excluded (its pom references `org.eclipse.swt.${osgi.platform}`). Needs JavaSE-21. |
| `edu.wpi.first.thirdparty.frc2023.opencv:opencv-java` | 4.6.0-5 | app | `scale/ResizerNewOpenCV` | **Small classes-only jar** from the WPILib repo `https://frcmaven.wpi.edu/artifactory/release/` (`checksumPolicy=fail`), byte-identical to the former local install `org.opencv:opencv:4.6.0` (326,063 B, SHA-256 `dd74cfd431ef56eb873ce8f81966c4351cbf7f865b798077a2b060787ac697b6`); the owner's copy `xanderView/opencv/opencv-460.jar` and `register.cmd` are no longer needed. The 100 MB+ jar the owner avoids is a bundled-natives distribution.<br>Native: the custom `opencv_java460.dll`, which needs the MSVC 2015–2022 runtime and Media Foundation DLLs from the OS. |
| `com.carrotsearch:hppc` | 0.8.2 | app | `OrientationStore`, `ImageCanvas`, `Selector` | |
| `com.jhlabs:filters` | 2.0.235-1 | thirddep (provided), app | vendored `com.mortennobel.imagescaling.AdvancedResizeOp` (unsharpen) | Required to compile thirddep; the app declares it for runtime |
| `net.jpountz.lz4:lz4` | 1.3.0 | app | `hash/Hasher` (xxHash64) | The JNI part is optional (pure-Java fallback). Successor: `org.lz4:lz4-java` (same packages). |
| `me.xuender:unidecode` | 0.0.7 | app | `file/entry/EncodeName` | |
| `com.github.umjammer.vavi-commons:vavi-commons` | 1.1.14 | thirddep (jitpack) | `vavi.util.Debug` (about 20 calls in the vendored vavi code) | **Fixed in P1.2.** Java 17 bytecode; registers `java.net` services (`classpath:` URL handler). |
| ~~`com.github.umjammer:vavi-image`~~ | ~~1.0.11~~ | **removed** (P1.3) | — | The only used class, `vavi.imageio.WrappedImageInputStream`, is vendored in thirddep. |
| `net.java.dev.jna:jna` | 5.15.0 | thirddep | vavi AVIF binding | |
| `org.slf4j:slf4j-api` | 1.7.10 | commons, app | commons and app logging (PLAN P3.4) | |
| `org.slf4j:slf4j-simple` | 1.7.10 | app | the only SLF4J binding (P1.8) | Config: `xanderView/src/main/resources/simplelogger.properties` |
| `junit:junit` | 4.8.2 | all (test) | nothing (0 `@Test`) | JUnit 5 is PLAN P3.1 |
| ~~`ch.qos.logback:logback-classic`, `javax.xml.bind:jaxb-api`, `commons-cli:commons-cli`, `org.jocl:jocl`, `net.coobird:thumbnailator`~~ | — | **removed** (P1.8, P2.2, P2.3) | — | Their only users were deleted (or the import was unused). |
Repositories: Central (declared first) and `wpilib` (OpenCV only) in the parent; `jitpack.io` (vavi-commons) in thirddep. The tmatesoft repo is gone.
Build plugins (versions pinned in the parent `pluginManagement`): compiler 3.13.0 (`release=26`), resources 3.4.0, jar 3.4.2, shade 3.6.0 (app only), surefire 3.5.4, install/deploy 3.1.4, clean 3.2.0, site 3.12.1, enforcer 3.6.3, dependency 3.11.0, build-helper 3.6.2, wrapper 3.3.4.

## 2. Vendored libraries in `thirddep-j7` (patched → `LibraryLoader`)
| Package | Upstream / version | Patch (load name) | Used by app | Replacement option that keeps the custom loading |
|---|---|---|---|---|
| ~~`org.eclipse.swt`, win32 flavor (`src/swt-win32/`)~~ | ~~Eclipse SWT 4.527 (2015) win32 Java sources + message bundles~~ | **deleted in P7 (2026-10-13)**: Windows uses the stock 3.135 overlay below; natives `swt-win32-4974r10.dll`, `swt-osversion-win32-4974r10.dll`, `swt-awt-win32-4974r10.dll`, `swt-gdip-win32-4974r10.dll` come from the Central jar via the Windows bundle | — | — |
| `org.eclipse.swt`, all flavors (`src/swt-stock/` = only `internal/Library.java`) | stock SWT 3.135.0 classes (unpacked at build time) + upstream 3.135.0 `Library.java` (identical for gtk, cocoa and win32) | `Library.loadLibrary(name, mapName)` → `LibraryLoader.tryLoadLibrary` for the upstream stems (`<name>-<platform>-<version>`, `<name>-<platform>`, `<name>`); `UnsatisfiedLinkError` if none loads (gtk's pi3/pi4 fallback relies on it); upstream `load`/`extract`/`extractResource`/`chmod` removed | the same public API (the app uses no SWT internals) | — |
| `net.sf.sevenzipjbinding` (77) | 7-Zip-JBinding **26.03-2.3** (7-Zip 26.03 engine) from the owner's fork [korvin2000/sevenzipjbinding](https://github.com/korvin2000/sevenzipjbinding) (`jbinding-java/src`, copied 2026-10-13; `impl/RandomAccessNioStream` is the owner's); natives from the fork's CI releases | `SevenZip.ensureLibraryIsInitialized` (`lib7-Zip-JBinding`, then `initLoadedLibraries()`) | `SevenZipReader`, `ImageZip` | Re-copy `jbinding-java/src` and re-apply the one patch when the fork moves |
| ~~`org.libjpegturbo.turbojpeg`~~ | ~~TurboJPEG Java API 3.1.x (JNI)~~ | **deleted 2026-10-13**: the app's `codec/TurboJpeg` binds `libturbojpeg` ≥ 3.0 through the FFM API (`turbojpeg`), so no JNI glue and any build works (official 3.2.0, the owner's fork's optimized 3.2.1). Benchmark below (§4) | `gui/ImageLoader` | — |
| `com.mortennobel.imagescaling` (21) | java-image-scaling 0.8.x | none (pure Java) | `ResizerNobel`, `ScaleFactory` (MultiStep) | `com.mortennobel:java-image-scaling:0.8.6` (safe: no natives) |
| `dev.matrixlab.webp4j` (8) | decoder subset of **webp4j 2.5.0** (MIT, JNI on libwebp 1.6.x): `WebPCodec.decodeImage/isAvailable`, `NativeWebP` (4 natives), `PixelConverter.wrapPixels`, the model classes the JNI library caches | `NativeWebP` static init → `LibraryLoader.loadLibrary("webp4j")` | `gui/ImageLoader` (WEBP case, no ImageIO SPI) | Natives: the owner's fork [korvin2000/libwebp](https://github.com/korvin2000/libwebp) CI builds for all six platforms (release `Release-1.6.0-2.5.0-ci1`; the stock Central jar natives are the documented fallback). The former `com.luciad.imageio.webp` + `webp-imageio` natives were deleted 2026-10-13. |
| `vavi.*` AVIF (JNA) | umjammer vavi-image-avif | `Avif` → `checkLibrary("libavif", false)` + JNA `avif` | ImageIO SPI (reader + writer) | Keep vendored (P4.3). |
| `vavi.imageio.jpegxl.jni` + `org.jpeg.jpegxl.wrapper` | JNI JXL wrapper (`jxl_jni`) | `JpegXLImageReader` (static: `libjxl_jni`) | ImageIO SPI (reader), the only JPEG XL path | Keep. |
| ~~`vavi.imageio.jpegxl.jna`, `vavi.awt.image.jna.jpegxl`, `vavi.image.jpegxl`~~ | vavi JNA JXL | — | **deleted** (P2.4; it was disabled) | — |
| ~~`no.nixx.opencl`~~ | — | — | **deleted** (P2.2) | — |
SPI registrations (`thirddep-j7/src/main/resources/META-INF/services/`): the readers are `vavi.imageio.avif.AvifImageReaderSpi` and `vavi.imageio.jpegxl.jni.JpegXLImageReaderSpi`; the writer is AVIF. WebP has no SPI (webp4j is called directly).
The owner's untracked JNA-JXL DLLs in `thirddep-j7/src/main/resources/win32-x86-64/` were deleted (2026-10-12, owner decision); copies of `jxl`, `jxl_jni` and `jxl_threads` remain in `xanderView/lib/jxl/`.

## 3. commons-j7 usage (what the app and thirddep import)
Used: `strings.StringUtils`, `io.FileUtils`, `jni.LibraryLoader`, `io.FilePath`, `types.DoubleValue`, `streams.Streams`, `base.Equals`, `strings.Args`, `io.AbstractFileProcessor` (+ `io.helpers.WildcardMatcher`), `utils.Metrics`, `io.TextFileUtils`, `utils.{RandomUtil,ByteUtils,BooleanUtils}`, `types.{BitNumber,Range,BitNumberRange}`, `strings.BogusComparator`, `datatypes.CheckSignature`, `io.ByteBufferFileUtils`, `collections.SortedLinkedList`, `base.{Numbers,CommonUtils}`, `io.SysInfo` (internal).
Deleted in P2.3: the packages `logger`, `net`, `reflection`, `ui`, `xml`, `io.zip`, and the copies of `org.apache.commons.io.*` and `org.eclipse.osgi.*`. Some unused classes remain in the kept packages.

## 4. Natives
<a id="natives"></a>
Since PLAN P7 the natives are **upstream builds pinned by SHA-256** in `natives/<platform>.tsv`, bundled by `tools/natives/bundle.sh` into `xview-natives-<platform>-<rev>.zip` (GitHub release `natives-<rev>`), downloaded by the build and shipped as `libs/<os>/<arch>/` next to the jar together with `natives.sha256` and `natives.deps`. Design, the 6-platform matrix with sources, loader modes and owner workflows: [NATIVES.md](NATIVES.md). The loader (`LibraryLoader`, [ARCHITECTURE §6](ARCHITECTURE.md)) copies each file on first use into the per-user cache `<AppData>/libs/<os>/<arch>/<bundle12>/`, verifies it against the manifest and trusts it afterwards; a directory without `natives.sha256` (the owner's deployed `C:\Program Files\xview\libs\windows\amd64\`) is served in the legacy exact-compare mode from the flat cache `%APPDATA%\xanderView\libs\windows\amd64\`.

**Source** when nothing is configured: the directory of the jar if it has `libs/<os>/<arch>/`, else the jar itself. `-Dxview.libs.location=<dir or URL>` or the environment variable `XVIEW_LIBS_LOCATION` override it (an explicit location is the owner's override: its own manifest is authoritative). `libs.properties` no longer carries an absolute path. None of the binaries are in git.

**File names:** a load name maps to `<name>.<ext>`; on Linux/macOS `lib<name>.<ext>` is tried too, and on macOS `.dylib` before `.jnilib` (JNA libraries only `.dylib`). In manifest mode the first candidate listed in `natives.sha256` wins. Windows: exactly `<name>.dll`.

### Windows x64: the natives of the tree `natives/windows/amd64/` (`natives/windows-x86_64.tsv`; the owner's deployed set is listed in the last column)
| Load name → file | API | Feature | Load site | Bundle (upstream) | Owner's deployed file |
|---|---|---|---|---|---|
| `swt-win32-4974r10` → `swt-win32-4974r10.dll` 791 KB, `swt-osversion-win32-4974r10` → 124 KB (both at startup), `swt-awt-win32-4974r10` → 122 KB (at `SWT_AWT.new_Frame`), `swt-gdip-win32-4974r10` → 171 KB (lazily, GDI+) | JNI | SWT UI (mandatory) | stock-SWT `internal/Library` (patched) via `tryLoadLibrary` | Maven Central `org.eclipse.platform:org.eclipse.swt.win32.win32.x86_64:3.135.0` (EPL-2.0) | the deployed `swt.dll` = stock Eclipse 4.5 `swt-win32-4527.dll` (legacy mode only) |
| `lib7-Zip-JBinding` → `lib7-Zip-JBinding.dll` 2.9 MB | JNI | zip/rar/7z/jzip | `SevenZip.ensureLibraryIsInitialized` | the owner's fork release `Release-26.03-2.3-ci3`, `Windows-amd64-avx2` zip (Clang 23.1.3 `-O3 -march=x86-64-v3 -ffast-math -flto`, needs AVX2; LGPL-2.1 + unRAR restriction) | 16.02-2.01 stock (incompatible with the 26.03 Java) |
| `turbojpeg` → `turbojpeg.dll` 1.1 MB | **FFM** | fast JPEG decode with DCT scaling | `codec/TurboJpeg` | the owner's fork release `win64-compilers-20261013-4`: libjpeg-turbo 3.2.1 `clang-nightly-avx2-lto-pgo` `bin/libturbojpeg.dll` (needs AVX2); any libturbojpeg ≥ 3.0 works | owner's VS2015 build of 3.0.91 (JNI exports unused now) |
| `webp4j` → `webp4j.dll` 764 KB | JNI | WebP decode | `NativeWebP` static init | the owner's fork release `Release-1.6.0-2.5.0-ci1`: webp4j 2.5.0 JNI library on the fork's libwebp 1.6.0, `webp4j-windows-x64-avx2.dll` = `clang-nightly-avx2-lto-pgo` (needs AVX2; MIT + BSD-3-Clause) | — (`webp-imageio.dll` is obsolete) |
| `libavif` → `libavif.dll` 3.9 MB | **JNA** (`Native.load("avif")`) | AVIF / AV1 decode | `Avif` (`checkLibrary(…, false)` sets `jna.library.path`) | the owner's MinGW build of libavif 1.1.1 with static dav1d + aom, AVX2 (TSV url `-`: taken from the tracked tree); SDL3_image 3.4.8 `libavif-16.dll` was the 2026.10 alternative | byte-identical |
| `libjxl_jni` → `libjxl_jni.dll` 1.5 MB | JNI | JPEG XL | `JpegXLImageReader` static init | the owner's self-contained AVX2 build (TSV url `-`); libjxl v0.12.0 `jxl-x64-windows-static.7z` was the 2026.10 alternative | byte-identical |
| `opencv_java460` → `opencv_java460.dll` 3.4 MB | JNI | CV_* resizers (`Scaler` types 4–7) | `ResizerNewOpenCV` static init | the owner's custom minimal 4.6.0 build, re-hosted on the natives release (`opencv_java460-windows-x86_64-custom.dll`; WPILib `opencv-jni:4.6.0-5:windowsx86-64`, 13.9 MB, is the documented alternative); **imports `CONCRT140.dll`** (not shipped by the JDK → VC++ 2015-2022 redistributable, or drop `concrt140.dll` into `libs/` = optional dependency) and Media Foundation (absent on Windows N) | byte-identical |
`libgcc_s_seh-1.dll` is no longer requested or shipped: no native imports it (PE import tables; PLAN B9). The owner's deployed directory still contains it harmlessly.

**Natives outside `LibraryLoader` (exceptions to the one-loader intent):**
- JNA extracts its own `com/sun/jna/win32-x86-64/jnidispatch.dll` from the jar (P7.8: `jna.boot.library.path`).

### TurboJPEG binding: JNI vs FFM vs JNA (measured 2026-10-13)
Ryzen 5 5500U, JDK 26, decode to `TYPE_3BYTE_BGR`, median of 15 (scratch `TjBench`, not in the repo):

| binding | 24 MP, denom 1 | 24 MP, denom 4 | 4 MP, denom 1 |
|---|---|---|---|
| vendored JNI Java + official 3.1.4.1 `turbojpeg.dll` (zero-copy via `GetPrimitiveArrayCritical`) | 125 ms | 99 ms | 22 ms |
| FFM, `Linker.Option.critical(true)` with heap arrays (zero-copy), same DLL | 130 ms | 99 ms | 22 ms |
| FFM, off-heap buffers + copies, same DLL | 150 ms | 106 ms | 24 ms |
| FFM critical, the fork's 3.2.1 `clang-nightly-avx2-lto-pgo` DLL | 129 ms | 99 ms | 22 ms |

- **Chosen: FFM critical** (`codec/TurboJpeg`): as fast as JNI, zero-copy, no JNI glue library, any libturbojpeg ≥ 3.0 build. Cost: a critical downcall skips the thread-state transition, so a safepoint requested by another thread waits until the decode returns (≤ the decode time; the decode already blocks the UI thread, PLAN P3.3).
- Off-heap FFM (GC-friendly) costs 8-20 %; **JNA** (the `TurboJPEG/JNA` sample of libjpeg-turbo 3.2, `turbojpeg-java` is JNI again) needs native buffers and a copy into the image, so it is bounded by the off-heap row plus JNA's marshalling; not measured, rejected.
- The AVX2/LTO/PGO build decodes 8-bit baseline JPEG exactly as fast as the official build: the hot loops are hand-written SIMD selected at run time. The fork's benchmark shows the gains elsewhere (+9 % progressive decode with PGO, 12-bit/lossless paths).
- lz4-java: `Hasher` uses `XXHashFactory.safeInstance()` since P7, so no JNI extraction happens (identical xxHash64 output).
- OS runtimes: MSVC runtime (`MSVCP140`, `VCRUNTIME140*`, UCRT come with the JDK; `CONCRT140` does not) and Media Foundation (`MFPlat`, `MF`, `MFReadWrite`) for OpenCV.
- SHA-256 values: `natives/windows-x86_64.tsv` (bundle) and [ADDITIONAL_INFO §4](ADDITIONAL_INFO.md) (owner's deployed set).

### Platform matrix
| Platform (`xview.platform`) | Loader dir | Bundle | Status |
|---|---|---|---|
| `windows-x86_64` | `libs/windows/amd64/` | 10 files, 7.5 MB zip (4 stock SWT DLLs; owner's AVX2 OpenCV, libavif, jxl_jni; fork builds of 7-Zip 26.03, libjpeg-turbo 3.2.1, webp4j) | runs; verified 2026-10-13 from the tracked tree (`--check-natives` OK, JPEG/WebP/zip/7z smoke test) |
| `linux-x86_64` | `libs/linux/x86_64/` | 12 files, 18.0 MB zip (SWT 6, 7-Zip 26.03 avx2 from the fork's new glibc workflow, TurboJPEG 3.2.0, webp4j from the fork, libavif 1.4.2, static jxl_jni 0.12.0 from our CI, OpenCV); glibc ≥ 2.35 | bundle built; **runtime unverified** |
| `linux-aarch64` | `libs/linux/aarch64/` | 11 files, 12.9 MB zip (no SWT `pi4`; 7-Zip 26.03 arm64 glibc) | same |
| `macos-x86_64` | `libs/macos/x86_64/` | 9 files, 14.0 MB zip (7-Zip 26.03 thin, TurboJPEG 3.2.0 universal, webp4j from the fork, jxl_jni from our CI); macOS ≥ 10.15 | same |
| `macos-aarch64` | `libs/macos/aarch64/` | 9 files, 10.8 MB zip (7-Zip 26.03 thin arm64, jxl_jni from our CI); macOS ≥ 11 | same |
| `windows-aarch64` | `libs/windows/aarch64/` | 12 files, 10.1 MB zip (SWT ×4, 7-Zip 26.03 arm64, TurboJPEG 3.2.0, webp4j, jxl_jni from our CI, OpenCV, libavif + `aom.dll` + `dav1d.dll`); **complete** since 2026-10-13 | cross-built dist verified, runtime unverified |
Per-file sources, versions and licenses: `natives/<platform>.tsv`; the research behind them: [NATIVES.md §2](NATIVES.md). Sibling dependencies of a native must be self-contained or listed in `natives.deps` (loaded first by absolute path); `System.load` from the cache directory does not add that directory to the dynamic linker path.

## 5. vavi artifacts: findings (2026-10-12)
- None of the vavi artifacts are on Maven Central; they are **jitpack only**.
- `com.github.umjammer:vavi-image`:
  - Tags run 1.0.2–1.0.15. Jitpack builds failed for 1.0.6, 1.0.7, 1.0.13 and 1.0.14.
  - **The 1.0.11 artifacts give 404** even though the jitpack API reports "ok". 1.0.12 and 1.0.15 resolve (HTTP 200).
  - 1.0.12 and later target Java 17 and pull in `vavi-commons` 1.1.10, `com.github.umjammer:jiu` 0.14.3 and `org.scijava:native-lib-loader` 2.5.0. native-lib-loader is a self-extracting native loader, which is against the design in §0.
  - `vavi.imageio.WrappedImageInputStream` is about 70 LOC with only `java.io` and `javax.imageio.stream` imports and no vavi dependencies, so it is trivial to vendor.
- `vavi-commons`: tags go up to 1.1.24, and every jitpack build is ok. The real 1.1.14 jar is at `com.github.umjammer.vavi-commons:vavi-commons`. It targets Java 17, which is fine on JDK 21 even when thirddep targets 16.
- Sister projects: vavi-image-avif 0.0.7 (Java 17, jna 5.14, vavi-image 1.0.12; writer marked TODO upstream), vavi-image-jpegxl 0.0.7 (JNA, vavi-image 1.0.13, which failed on jitpack), vavi-image-webp 0.2.6 (pure-Java decoder). The vendored copies are already adapted to LibraryLoader, so **upgrading them is optional and not needed for the build**.
- **Applied (PLAN P1.2–P1.3, 2026-10-12):**
  - vavi-commons now uses the real coordinates.
  - `WrappedImageInputStream` is vendored as `thirddep-j7/src/main/java/vavi/imageio/WrappedImageInputStream.java` (`// xview-patch`), and the `vavi-image` dependency is gone. With it go jiu and native-lib-loader.
  - Fallback if more vavi-image classes are ever needed: `com.github.umjammer:vavi-image:1.0.12`, excluding `native-lib-loader` and `jiu`.
