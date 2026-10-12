# Dependencies, vendored code, natives

> Verified 2026-10-12 against the poms, the source, `~/.m2`, the jitpack/Central/WPILib HTTP status, the deployed install `C:\Program Files\xview\` and the native cache `%APPDATA%\xanderView\libs\`. Versions come from the parent `pom.xml` `<properties>` unless noted.

## 0. Design constraint (read before changing any dependency)
xanderView **intentionally** vendors and patches SWT, 7-Zip-JBinding, TurboJPEG, WebP and the AVIF/JXL bindings. The goal is that **all natives go through one custom loader** (`net.j7.commons.jni.LibraryLoader`), which copies **custom, performance-optimized builds** (some built and copied by hand) once into a persistent per-user directory and loads them from there. Stock jars that bundle and self-extract natives for every OS contradict this design. The minified OpenCV jar exists for the same reason. Mechanism and paths: [ARCHITECTURE §6](ARCHITECTURE.md). Every replacement option below must keep this loading model.
The Linux/macOS SWT flavors use the **Java classes** of the stock SWT 3.135 jar, unpacked at build time without its natives and its loader; their natives also come only from `LibraryLoader` (§2, PLAN P5.1).

## 1. Maven dependencies
| Artifact | Version | Declared in (scope) | Actually used by | Notes |
|---|---|---|---|---|
| ~~`org.eclipse.swt:org.eclipse.swt.win32.win32.x86_64`~~ | ~~4.3~~ | **removed** (PLAN P1.11) | — | It duplicated the vendored SWT **4.527** sources. |
| `org.eclipse.platform:org.eclipse.swt.<ws>.<os>.<arch>` | 3.135.0 (`swt.stock.version`) | thirddep, **build-time unpack only** (`maven-dependency-plugin:unpack`, non-Windows `swt-*` profiles) | SWT gtk/cocoa Java classes | Not a dependency: natives, `META-INF` signatures and the stock `org.eclipse.swt.internal.Library` are excluded; the patched `Library` comes from `thirddep-j7/src/swt-stock/`. Needs JavaSE-21. |
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
Build plugins (versions pinned in the parent `pluginManagement`): compiler 3.13.0 (`release=21`), resources 3.4.0, jar 3.4.2, shade 3.6.0 (app only), surefire 3.5.4, install/deploy 3.1.4, clean 3.2.0, site 3.12.1, enforcer 3.6.3, dependency 3.11.0, build-helper 3.6.2, wrapper 3.3.4.

## 2. Vendored libraries in `thirddep-j7` (patched → `LibraryLoader`)
| Package | Upstream / version | Patch (load name) | Used by app | Replacement option that keeps the custom loading |
|---|---|---|---|---|
| `org.eclipse.swt`, win32 flavor (`src/swt-win32/`) | Eclipse SWT 4.527 (Eclipse 4.5 "Mars", 2015), **win32 flavor** of the Java code; message bundles in `src/swt-win32/resources/`. The `browser`, `internal.mozilla`, `internal.webkit`, `opengl` and `internal.opengl.win32` packages were deleted (P2.4); `ole.win32` stays (used by `accessibility`). | `internal/Library.loadLibrary` → `LibraryLoader` (`swt`; `swt-gdip` and `swt-awt` soft-fail) | widgets, graphics, layout, `awt.SWT_AWT` (reflects into `sun.awt.windows`), `program.Program` | Newer SWT needs a matching `swt-win32-<ver>.dll` (PLAN P4.2). |
| `org.eclipse.swt`, gtk/cocoa flavors (`src/swt-stock/` = only `internal/Library.java`) | stock SWT 3.135.0 classes (unpacked at build time) + upstream 3.135.0 `Library.java` (identical for gtk and cocoa) | `Library.loadLibrary(name, mapName)` → `LibraryLoader.tryLoadLibrary` for the upstream stems (`<name>-<platform>-<version>`, `<name>-<platform>`, `<name>`); `UnsatisfiedLinkError` if none loads (gtk's pi3/pi4 fallback relies on it); upstream `load`/`extract`/`extractResource`/`chmod` removed | the same public API (the app uses no SWT internals) | — |
| `net.sf.sevenzipjbinding` (71) | 7-Zip-JBinding **16.02-2.01** (`SevenZip.SEVENZIPJBINDING_VERSION`), the latest upstream release; the DLL's 27 JNI exports match | `SevenZip.ensureLibraryIsInitialized` (`lib7-Zip-JBinding`, `libgcc_s_seh-1`, then `initLoadedLibraries()`) | `SevenZipReader`, `ImageZip` | Already current. Upstream also supports a manual `System.load` + `SevenZip.initLoadedLibraries()`, so the core jar could replace the vendored copy |
| `org.libjpegturbo.turbojpeg` (15 main) | TurboJPEG Java API matching **libjpeg-turbo 3.0.91** (3.1 beta, build 20240924): its native methods equal the JNI exports of `turbojpeg.dll` 1:1 | `TJLoader` → `LibraryLoader.loadLibrary("turbojpeg")` (direct since P5.1; was via SWT `Library`) | `gui/ImageLoader` | Keep vendored; the 3.1.x final Java API needs a rebuilt DLL first (PLAN P4.2) |
| `com.mortennobel.imagescaling` (21) | java-image-scaling 0.8.x | none (pure Java) | `ResizerNobel`, `ScaleFactory` (MultiStep) | `com.mortennobel:java-image-scaling:0.8.6` (safe: no natives) |
| `com.luciad.imageio.webp` (10) | webp-imageio (2013), JNI | `WebP.loadNativeLibrary` (`webp-imageio`) | ImageIO SPI | Keep (custom DLL; owner decision) |
| `vavi.*` AVIF (JNA) | umjammer vavi-image-avif | `Avif` → `checkLibrary("libavif", false)` + JNA `avif` | ImageIO SPI (reader + writer) | Keep vendored (P4.3). |
| `vavi.imageio.jpegxl.jni` + `org.jpeg.jpegxl.wrapper` | JNI JXL wrapper (`jxl_jni`) | `JpegXLImageReader` (static: `libjxl_jni`) | ImageIO SPI (reader), the only JPEG XL path | Keep. |
| ~~`vavi.imageio.jpegxl.jna`, `vavi.awt.image.jna.jpegxl`, `vavi.image.jpegxl`~~ | vavi JNA JXL | — | **deleted** (P2.4; it was disabled) | — |
| ~~`no.nixx.opencl`~~ | — | — | **deleted** (P2.2) | — |
SPI registrations (`thirddep-j7/src/main/resources/META-INF/services/`): the readers are `vavi.imageio.avif.AvifImageReaderSpi`, `com.luciad.imageio.webp.WebPImageReaderSpi` and `vavi.imageio.jpegxl.jni.JpegXLImageReaderSpi`; the writers are AVIF and WebP.
The owner's untracked JNA-JXL DLLs in `thirddep-j7/src/main/resources/win32-x86-64/` were deleted (2026-10-12, owner decision); copies of `jxl`, `jxl_jni` and `jxl_threads` remain in `xanderView/lib/jxl/`.

## 3. commons-j7 usage (what the app and thirddep import)
Used: `strings.StringUtils`, `io.FileUtils`, `jni.LibraryLoader`, `io.FilePath`, `types.DoubleValue`, `streams.Streams`, `base.Equals`, `strings.Args`, `io.AbstractFileProcessor` (+ `io.helpers.WildcardMatcher`), `utils.Metrics`, `io.TextFileUtils`, `utils.{RandomUtil,ByteUtils,BooleanUtils}`, `types.{BitNumber,Range,BitNumberRange}`, `strings.BogusComparator`, `datatypes.CheckSignature`, `io.ByteBufferFileUtils`, `collections.SortedLinkedList`, `base.{Numbers,CommonUtils}`, `io.SysInfo` (internal).
Deleted in P2.3: the packages `logger`, `net`, `reflection`, `ui`, `xml`, `io.zip`, and the copies of `org.apache.commons.io.*` and `org.eclipse.osgi.*`. Some unused classes remain in the kept packages.

## 4. Natives
<a id="natives"></a>
**Source** (copied from when the cache file is missing or its content differs; see [ARCHITECTURE §6](ARCHITECTURE.md)): `C:\Program Files\xview\libs\windows\amd64\`, because `commons-j7/src/main/resources/libs.properties` has `location=C:/Program Files/xview`. `-Dxview.libs.location` or the environment variable `XVIEW_LIBS_LOCATION` override it (PLAN P1.9). **Persistent cache** (loaded from): `%APPDATA%\xanderView\libs\windows\amd64\`. The deployed app is `C:\Program Files\xview\xanderview.jar`. None of these binaries are in git.

**File names:** a load name maps to `<name>.<ext>`; on Linux/macOS `lib<name>.<ext>` is tried too, and on macOS `.dylib` before `.jnilib` (JNA libraries only `.dylib`). A file present in the source beats one present only in the cache (PLAN P5.2). Windows: exactly `<name>.dll`.

### Windows x64: the 8 required DLLs (all present in both the source and the cache)
| Load name → file | API | Feature | Load site | Notes |
|---|---|---|---|---|
| `swt` → `swt.dll` (644 KB) | JNI | SWT UI (mandatory) | SWT `internal/C`, `internal/win32/OS` (win32 flavor) | Custom build. There is no `swt-gdip.dll`/`swt-awt.dll` in the set; those loads soft-fail.<br>PE inspection ([ADDITIONAL_INFO §4](ADDITIONAL_INFO.md)) found **no** exported GDI+ or AWT-bridge symbols. `SWT_AWT.new_Frame`, the path the app uses, works by reflection into `WEmbeddedFrame` and needs no native AWT. Code that uses the GDI+ (advanced GC) or `SWT_AWT.new_Shell` paths is unvalidated. |
| `lib7-Zip-JBinding` → `lib7-Zip-JBinding.dll` (5.5 MB) | JNI | zip/rar/7z/jzip | `SevenZip.ensureLibraryIsInitialized` | MinGW build |
| `libgcc_s_seh-1` → `libgcc_s_seh-1.dll` | JNI (`System.load`) | MinGW runtime, stated by the owner as a libavif dependency; neither DLL imports it directly (PE inspection) | `SevenZip` init loads it; `LibraryLoader` also copies it next to `libavif` (Windows dependency map) | B9: libavif 1.1.1 loads **without** it (verified on a fresh cache); the copy is preventive |
| `turbojpeg` → `turbojpeg.dll` (973 KB) | JNI | fast JPEG decode with DCT scaling | `TJLoader` (direct `LibraryLoader` call) | libjpeg-turbo 3.0.91 (3.1 beta, build 20240924). Falls back to ImageIO |
| `webp-imageio` → `webp-imageio.dll` (889 KB) | JNI | WebP read/write | `WebP.loadNativeLibrary` | |
| `libavif` → `libavif.dll` (3.9 MB) | **JNA** (`Native.load("avif")`, Windows tries the `lib` prefix) | AVIF / AV1 | `Avif` (`checkLibrary(…, false)` copies it and sets `jna.library.path`) | Probably statically linked codecs |
| `libjxl_jni` → `libjxl_jni.dll` (1.5 MB) | JNI | JPEG XL | `JpegXLImageReader` static init | Self-contained custom build. The `jxl*.dll`/`brotli*.dll` in `thirddep-j7/src/main/resources/win32-x86-64/` belonged to the removed JNA path and were deleted (PLAN P2.4). |
| `opencv_java460` → `opencv_java460.dll` (3.4 MB) | JNI | CV_* resizers (`Scaler` types 4–7) | `ResizerNewOpenCV` static init | Custom minimal build that pairs with the minified jar |
**Natives outside `LibraryLoader` (exceptions to the one-loader intent):**
- JNA extracts its own `com/sun/jna/win32-x86-64/jnidispatch.dll` from the jar.
- lz4-java's xxHash may try a JNI library through its own temp extraction. The deployed jar has no lz4 natives, so it falls back to Java.
- OS runtimes are needed: MSVC runtime (`MSVCP140`, `VCRUNTIME140*`, `CONCRT140`) and Media Foundation (`MFPlat`, `MF`, `MFReadWrite`) for OpenCV.
- SHA-256 values of all 8 DLLs: [ADDITIONAL_INFO §4](ADDITIONAL_INFO.md).

Extra files that are present but not loaded: `jpeg62.dll` and `jxl_jni.dll` in the source dir; `swt-awt`/`swt-gdip`/`swt-wgl`/`swt-xulrunner`/`opencv_java300`/`liblz4-java` in the test resources.
Other local copies (untracked): `thirddep-j7/src/test/resources/libs/<os>/<arch>/` has the same layout and acts as a partial source tree. There are also archives in `xanderView/lib/*.7z` (password in `xanderView/lib/info.txt`), `xanderView/lib/jxl/`, and older bundles (`dlls_2017-06-23.7z`, `amd64*.7z`).

### Platform matrix (target: Windows, Linux, macOS on x86_64 required; aarch64 optional)
| Platform dir | Natives available today | Status / names needed (PLAN P5.3) |
|---|---|---|
| `windows/amd64` | **complete** (8/8) | runs |
| `linux/x86_64`, `linux/aarch64` | `x86_64`: `libturbojpeg.so`, `liblz4-java.so` (test resources) | The jar builds (`-Dswt.flavor=gtk.linux.*`). Needed: SWT `libswt-gtk-4974r10.so`, `libswt-pi3-gtk-4974r10.so`, `libswt-cairo-gtk-4974r10.so`, `libswt-awt-gtk-4974r10.so`, `libswt-atk-gtk-4974r10.so` (lazy, accessibility); `lib7-Zip-JBinding.so`, `libturbojpeg.so`, `webp-imageio.so` or `libwebp-imageio.so`, `libavif.so` (JNA), `libjxl_jni.so`, `libopencv_java460.so` |
| `macos/x86_64`, `macos/aarch64` | `x86_64`: `lib7-Zip-JBinding.jnilib`, `libturbojpeg.jnilib`, `liblz4-java.jnilib` (test resources) | The jar builds (`-Dswt.flavor=cocoa.macosx.*`). Needed: SWT `libswt-cocoa-4974r10.jnilib`, `libswt-pi-cocoa-4974r10.jnilib`, `libswt-awt-cocoa-4974r10.jnilib`; the others as on Linux with `.dylib` or `.jnilib` (`libavif.dylib` must be `.dylib` for JNA). `PlatformLaunch` adds `-XstartOnFirstThread` |
Sibling dependencies of a custom native must be linked statically or found via `$ORIGIN`/`@loader_path` rpaths: `System.load` from the cache directory does not add that directory to the dynamic linker path.

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
