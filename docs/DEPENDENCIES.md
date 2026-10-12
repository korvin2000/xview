# Dependencies, vendored code, natives

> Verified 2026-10-12 against the poms, the source, `~/.m2`, the jitpack/Central HTTP status, the deployed install `C:\Program Files\xview\` and the native cache `%APPDATA%\xanderView\libs\`. Versions come from the parent `pom.xml` `<properties>` unless noted.

## 0. Design constraint (read before changing any dependency)
xanderView **intentionally** vendors and patches SWT, 7-Zip-JBinding, TurboJPEG, WebP and the AVIF/JXL bindings. The goal is that **all natives go through one custom loader** (`net.j7.commons.jni.LibraryLoader`), which copies **custom, performance-optimized builds** (some built and copied by hand) once into a persistent per-user directory and loads them from there. Stock jars that bundle and self-extract natives for every OS contradict this design. The minified OpenCV jar exists for the same reason. Mechanism and paths: [ARCHITECTURE §6](ARCHITECTURE.md). Every replacement option below must keep this loading model.

## 1. Maven dependencies
| Artifact | Version | Declared in (scope) | Actually used by | Notes |
|---|---|---|---|---|
| ~~`org.eclipse.swt:org.eclipse.swt.win32.win32.x86_64`~~ | ~~4.3~~ | **removed** (PLAN P1.11) | — | It duplicated the vendored SWT **4.527** sources. 4.527 is kept because it matches the custom `swt.dll` (which identifies itself as `swt-win32-4527`). The jar's `SWTMessages*.properties` were moved into `thirddep-j7/src/main/resources`. |
| `org.opencv:opencv` | 4.6.0 | thirddep (provided), app | `scale/ResizerNewOpenCV`, `ResizerOpenCV` | **Small classes-only jar** (`xanderView/opencv/opencv-460.jar`, 326,063 B, SHA-256 `dd74cfd4…697b6`, untracked).<br>Its manifest identifies it as **WPILib** `opencv-java` `4.6.0-5` (FRC Maven; see `xanderView/lib/opencv_link.txt`).<br>It is registered locally under the coordinate `org.opencv:opencv:4.6.0` by `xanderView/opencv/register.cmd` (`mvn install:install-file`; run it from that folder). The 100 MB+ jar the owner avoids is a bundled-natives distribution.<br>Native: the custom `opencv_java460.dll`, which needs the MSVC 2015–2022 runtime and Media Foundation DLLs from the OS. |
| `org.jocl:jocl` | 0.2.0-RC00 | thirddep, app (provided) | `no.nixx.opencl`, `scale/deprecated/ResizeOpenCL`, `orientation/RotatorGPU` | Dead path at runtime |
| `com.carrotsearch:hppc` | 0.8.2 | app | `OrientationStore`, `ImageCanvas`, `Selector` | |
| `com.jhlabs:filters` | 2.0.235-1 | thirddep (provided), app | vendored `com.mortennobel.imagescaling.AdvancedResizeOp` (unsharpen) | Required to compile thirddep |
| `net.coobird:thumbnailator` | 0.4.8 | thirddep, app (provided) | `scale/others/CustomScalers` (unreferenced) | Removal candidate |
| `net.jpountz.lz4:lz4` | 1.3.0 | app | `hash/Hasher` (xxHash64) | The JNI part is optional (pure-Java fallback). Successor: `org.lz4:lz4-java` (same packages). |
| `me.xuender:unidecode` | 0.0.7 | app | `file/entry/EncodeName`, `gui/Transliterator` | |
| `com.github.umjammer.vavi-commons:vavi-commons` | 1.1.14 | thirddep (jitpack) | `vavi.util.Debug` (about 20 calls in the vendored vavi code) | **Fixed in P1.2.** The old `com.github.umjammer:vavi-commons` was a 282 B aggregator pom. Java 17 bytecode; registers `java.net` services (`classpath:` URL handler), which the old deployed jar had too. |
| ~~`com.github.umjammer:vavi-image`~~ | ~~1.0.11~~ | **removed** (P1.3) | — | The only used class, `vavi.imageio.WrappedImageInputStream`, is vendored in thirddep (1.0.11 verbatim plus an `available()` fix). The jitpack 1.0.11 artifact gives 404. |
| `net.java.dev.jna:jna` | 5.15.0 | thirddep | vavi AVIF (and the unused JXL-JNA) bindings | |
| `org.slf4j:slf4j-api` / `slf4j-simple` | 1.7.10 | commons, app | commons only (the app uses `System.out`) | slf4j-simple **and** logback together give two bindings |
| `ch.qos.logback:logback-classic` | 1.1.2 | commons, app (provided) | `commons/utils/DateUtils` (one import) | |
| `javax.xml.bind:jaxb-api` | 2.3.1 | commons | `commons/xml/ReaderJAXB` (dead package) | |
| `commons-cli:commons-cli` | 1.3.1 | commons | nothing | Remove |
| `junit:junit` | 4.8.2 | all (test) | nothing (0 `@Test`) | |
| (undeclared) `net.sourceforge.jiu` | — | — | — | **Fixed in P1.1:** unused IDE auto-imports, left over from vavi-image's transitive `jiu`, were removed. |
Repositories: `jitpack.io` (thirddep: vavi-*) and `maven.tmatesoft.com` (commons, unused; remove it). OpenCV comes only from the local `~/.m2` install.

## 2. Vendored libraries in `thirddep-j7` (patched → `LibraryLoader`)
| Package | Upstream / version | Patch (load name) | Used by app | Replacement option that keeps the custom loading |
|---|---|---|---|---|
| `org.eclipse.swt` (793 files) | Eclipse SWT 4.527 (Eclipse 4.5 "Mars", 2015), **win32 flavor** of the Java code. The only SWT on the classpath (the 4.3 artifact was removed); message bundles live in `src/main/resources/org/eclipse/swt/internal/` | `internal/Library.loadLibrary` → `LibraryLoader` (`swt`; `swt-gdip` and `swt-awt` soft-fail) | widgets, graphics, layout, `awt.SWT_AWT` (reflects into `sun.awt.windows`), `program.Program`, `internal.win32.OS`/`SIZE` | Newer SWT honors `-Dswt.library.path` but expects versioned names (`swt-win32-<ver>.dll`). Linux and macOS need the gtk/cocoa Java code (PLAN P5). |
| `net.sf.sevenzipjbinding` (71) | 7-Zip-JBinding 9.20-era | `SevenZip.ensureLibraryIsInitialized` (`lib7-Zip-JBinding`, `libgcc_s_seh-1`, then `initLoadedLibraries()`) | `SevenZipReader`, `ImageZip` | Upstream 16.02 also supports a manual `System.load` + `SevenZip.initLoadedLibraries()`, so the core jar could be used without the platform jar |
| `org.libjpegturbo.turbojpeg` (15 main) | libjpeg-turbo TurboJPEG Java API. The copyright runs to 2024, and `decompress8`, `setScalingFactor` and `TJ.PARAM_*` are already the modern 3.x shape. Exact upstream revision unknown. | `TJLoader` → SWT `Library.loadLibrary("turbojpeg")` | `gui/ImageLoader` | Keep it vendored. Establish the upstream revision before any upgrade. |
| `com.mortennobel.imagescaling` (21) | java-image-scaling 0.8.x | none (pure Java) | `ResizerNobel`, `ScaleFactory` (MultiStep) | `com.mortennobel:java-image-scaling:0.8.6` (safe: no natives) |
| `com.luciad.imageio.webp` (10) | webp-imageio (2013), JNI | `WebP.loadNativeLibrary` (`webp-imageio`) | ImageIO SPI | Keep (custom DLL). Alternative: pure-Java `vavi-image-webp` 0.2.6 (decode-only; VP8L, alpha and speed unverified) |
| `vavi.*` AVIF (JNA) | umjammer vavi-image-avif | `Avif` → `checkLibrary("libavif", false)` + JNA `avif` | ImageIO SPI (reader + writer) | Keep vendored. Upstream 0.0.7 needs Java 17, jna 5.14, vavi-image 1.0.12. |
| `vavi.imageio.jpegxl.jni` + `vavi.image.jpegxl` + `org.jpeg.jpegxl.wrapper` | JNI JXL wrapper (`jxl_jni`) | `JpegXLImageReader` (static: `libjxl_jni`) | ImageIO SPI (reader) | Keep. Upstream vavi-image-jpegxl 0.0.7 is JNA-based and depends on vavi-image 1.0.13, whose jitpack build **failed**. |
| `vavi.imageio.jpegxl.jna` + `vavi.awt.image.jna.jpegxl` | vavi JNA JXL (`jxl`, `jxl_threads`, `encode`) | — | **disabled** (SPI line commented out) | Removal candidate |
| `no.nixx.opencl` (6) | small OpenCL rotator, unknown origin | — | `RotatorCPU`/`RotatorGPU` (both unreferenced) | Removal candidate |
SPI registrations (`thirddep-j7/src/main/resources/META-INF/services/`): the readers are `vavi.imageio.avif.AvifImageReaderSpi`, `com.luciad.imageio.webp.WebPImageReaderSpi` and `vavi.imageio.jpegxl.jni.JpegXLImageReaderSpi`; the writers are AVIF and WebP.
Trash: `src/main/java/{chrome.manifest,swt.js,swt.xpt,version.txt}` duplicate `src/main/resources`. These are Mozilla browser leftovers; the app doesn't use SWT Browser.

## 3. commons-j7 usage (what the app and thirddep import)
Used: `strings.StringUtils`(14), `io.FileUtils`(12), `jni.LibraryLoader`(11), `io.FilePath`(6), `types.DoubleValue`(5), `streams.Streams`(5), `base.Equals`(5), `strings.Args`(4), `io.AbstractFileProcessor`, `utils.Metrics`, `io.TextFileUtils`, `utils.{RandomUtil,ByteUtils,BooleanUtils}`, `types.{BitNumber,Range,BitNumberRange}`, `strings.BogusComparator`, `datatypes.CheckSignature`, `io.ByteBufferFileUtils`, `collections.SortedLinkedList`, `base.{Numbers,CommonUtils}`, `io.SysInfo` (internal).
Not imported by any consumer: the packages `logger`, `net`, `reflection`, `ui`, `xml`, `io.zip`, `io.helpers`, plus the copies of `org.apache.commons.io.*` and `org.eclipse.osgi.*`. Same-package and wildcard uses were not fully checked, so grep before deleting.

## 4. Natives
<a id="natives"></a>
**Source** (copied from when the cache file is missing or its content differs; see [ARCHITECTURE §6](ARCHITECTURE.md)): `C:\Program Files\xview\libs\windows\amd64\`, because `commons-j7/src/main/resources/libs.properties` has `location=C:/Program Files/xview`. **Persistent cache** (loaded from): `%APPDATA%\xanderView\libs\windows\amd64\`. The deployed app is `C:\Program Files\xview\xanderview.jar`. None of these binaries are in git.

### Windows x64: the 8 required DLLs (all present in both the source and the cache)
| Load name → file | API | Feature | Load site | Notes |
|---|---|---|---|---|
| `swt` → `swt.dll` (644 KB) | JNI | SWT UI (mandatory) | SWT `internal/C`, `internal/win32/OS` | Custom build. There is no `swt-gdip.dll`/`swt-awt.dll` in the set; those loads soft-fail.<br>PE inspection ([ADDITIONAL_INFO §4](ADDITIONAL_INFO.md)) found **no** exported GDI+ or AWT-bridge symbols. `SWT_AWT.new_Frame`, the path the app uses, works by reflection into `WEmbeddedFrame` and needs no native AWT. Code that uses the GDI+ (advanced GC) or `SWT_AWT.new_Shell` paths is unvalidated. |
| `lib7-Zip-JBinding` → `lib7-Zip-JBinding.dll` (5.5 MB) | JNI | zip/rar/7z/jzip | `SevenZip.ensureLibraryIsInitialized` | MinGW build |
| `libgcc_s_seh-1` → `libgcc_s_seh-1.dll` | JNI (`System.load`) | MinGW runtime, stated by the owner as a libavif dependency; neither DLL imports it directly (PE inspection) | `SevenZip` init loads it; `LibraryLoader` also copies it next to `libavif` (Windows dependency map) | B9: libavif 1.1.1 loads **without** it (verified on a fresh cache); the copy is preventive |
| `turbojpeg` → `turbojpeg.dll` (973 KB) | JNI | fast JPEG decode with DCT scaling | `TJLoader` | Falls back to ImageIO |
| `webp-imageio` → `webp-imageio.dll` (889 KB) | JNI | WebP read/write | `WebP.loadNativeLibrary` | |
| `libavif` → `libavif.dll` (3.9 MB) | **JNA** (`Native.load("avif")`, Windows tries the `lib` prefix) | AVIF / AV1 | `Avif` (`checkLibrary(…, false)` copies it and sets `jna.library.path`) | Probably statically linked codecs |
| `libjxl_jni` → `libjxl_jni.dll` (1.5 MB) | JNI | JPEG XL | `JpegXLImageReader` static init | Self-contained custom build. The `jxl*.dll`/`brotli*.dll` in `thirddep-j7/src/main/resources/win32-x86-64/` belong to the disabled JNA path. |
| `opencv_java460` → `opencv_java460.dll` (3.4 MB) | JNI | CV_* resizers (`Scaler` types 4–7) | `ResizerNewOpenCV` static init | Custom minimal build that pairs with the minified jar |
**Natives outside `LibraryLoader` (exceptions to the one-loader intent):**
- JNA extracts its own `com/sun/jna/win32-x86-64/jnidispatch.dll` from the jar.
- lz4-java's xxHash may try a JNI library through its own temp extraction. The deployed jar has no lz4 natives, so it falls back to Java.
- OS runtimes are needed: MSVC runtime (`MSVCP140`, `VCRUNTIME140*`, `CONCRT140`) and Media Foundation (`MFPlat`, `MF`, `MFReadWrite`) for OpenCV.
- SHA-256 values of all 8 DLLs: [ADDITIONAL_INFO §4](ADDITIONAL_INFO.md).

Extra files that are present but not loaded: `jpeg62.dll` and `jxl_jni.dll` in the source dir; `swt-awt`/`swt-gdip`/`swt-wgl`/`swt-xulrunner`/`opencv_java300`/`liblz4-java` in the test resources.
Other local copies (untracked): `thirddep-j7/src/test/resources/libs/<os>/<arch>/` has the same layout and acts as a partial source tree. There are also archives in `xanderView/lib/*.7z` (password in `xanderView/lib/info.txt`), `xanderView/lib/jxl/`, and older bundles (`dlls_2017-06-23.7z`, `amd64*.7z`).

### Platform matrix (target: Windows, Linux, macOS on x86_64 required; aarch64 optional)
| Platform dir | Natives available today | Other blockers |
|---|---|---|
| `windows/amd64` | **complete** (8/8) | — |
| `linux/x86_64` | `libturbojpeg.so`, `liblz4-java.so` (test resources) | SWT Java code is win32-only; no swt/7z/webp/avif/jxl/opencv `.so`; the TJLoader name `turbojpeg` ≠ file `libturbojpeg` |
| `macos/x86_64` | `lib7-Zip-JBinding.jnilib`, `libturbojpeg.jnilib`, `liblz4-java.jnilib` | Same SWT issue (cocoa); SWT_AWT embedding is limited on macOS; JNA needs `.dylib`, but LibraryLoader copies `.jnilib`; the runtime flag must use `sun.lwawt.macosx`, not `sun.awt.windows` |
| `*/aarch64` | none | LibraryLoader maps the dir name from `os.arch` (`aarch64`); nothing has been built |

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
