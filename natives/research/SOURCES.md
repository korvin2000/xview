# Native library sources found in the P7 research (2026-10-12/13)

Everything that was found, verified or rejected while assembling the bundles, so that nothing has to be searched twice.
"Verified" = downloaded, SHA-256 taken, JNI exports / imports inspected (hashes: [hashes-2026-10.txt](hashes-2026-10.txt)).
The chosen rows are in `natives/<platform>.tsv`; the downloaded assets are cached by SHA-256 in `~/.cache/xview-natives`
(125 MB, 38 files; `tools/natives/bundle.sh` reads it, so rebundling works offline). Research reports (Codex, Opus) are
summarized in [plan-review-2026-10.md](plan-review-2026-10.md) and [docs/NATIVES.md](../../docs/NATIVES.md).

## SWT 3.135.0 (used on all 6 platforms)
Maven Central `org.eclipse.platform:org.eclipse.swt.<ws>.<os>.<arch>:3.135.0`, natives inside the jar:
- `win32.win32.x86_64`: `swt-win32-4974r10.dll`, `swt-osversion-…`, `swt-awt-…`, `swt-gdip-…` (+ `swt-wgl`, `WebView2Loader`, not needed)
- `win32.win32.aarch64`: same set, arm64
- `gtk.linux.x86_64`: `libswt-{gtk,pi3,pi4,cairo,atk,awt,glx,webkit}-gtk-4974r10.so` (bundled: gtk, pi3, pi4, cairo, atk, awt)
- `gtk.linux.aarch64`: same without `pi4`
- `cocoa.macosx.{x86_64,aarch64}`: `libswt-{cocoa,pi-cocoa,awt-cocoa}-4974r10.jnilib`
Historical: the owner's `swt.dll` = Eclipse 4.5 `swt-win32-4527.dll` from `https://archive.eclipse.org/eclipse/downloads/drops4/R-4.5-201506032000/swt-4.5-win32-win32-x86_64.zip` (`swt.jar!swt-win32-4527.dll`, sha256 3377b359…).

## 7-Zip-JBinding 16.02-2.01 (vendored Java matches; 27 `Java_net_sf_sevenzipjbinding_*` exports)
- Central `net.sf.sevenzipjbinding:sevenzipjbinding-all-platforms:16.02-2.01` → `Windows-amd64/`, `Linux-amd64/`, `Mac-x86_64/` (+ `Windows-x86`, `Linux-i386`) — verified, self-contained
- Central `net.sf.sevenzipjbinding:sevenzipjbinding-linux-arm64:16.02-2.01` → `Linux-arm64/lib7-Zip-JBinding.so` — verified (glibc 2.17)
- muCommander patched jar (Apple Silicon): `https://raw.githubusercontent.com/mucommander/mucommander.github.io/master/maven/com/mucommander/sevenzipjbinding-all-platforms/16.02-2.01/sevenzipjbinding-all-platforms-16.02-2.01.jar` → `Mac-aarch64/lib7-Zip-JBinding.dylib` — verified (28 exports, libSystem + libc++ only); `master` is a moving ref, pinned by hash
- SourceForge official zips: `https://sourceforge.net/projects/sevenzipjbind/files/7-Zip-JBinding/16.02-2.01/` (same binaries)
- **Windows arm64: nothing for 16.02** (only `sevenzipjbinding-26.03-2.3-Windows-arm64.zip` in SourceForge OldFiles, a newer binding family with a different JNI interface — not a drop-in). → owner builds 16.02-2.01 for arm64 (sources: `https://github.com/borisbrodski/sevenzipjbinding/tree/Release-16.02-2.01`, CMake + MinGW/clang-arm64).
- Newer official family (needs new Java too): 23.01-2.2 universal macOS zip `https://github.com/borisbrodski/sevenzipjbinding/releases/download/Release-23.01-2.2/sevenzipjbinding-23.01-2.2-Mac.zip`.

## libjpeg-turbo / TurboJPEG (vendored Java = 3.0.91/3.1.x JNI API, 39 exports)
- **3.1.4.1 official (used, all 6):** `https://github.com/libjpeg-turbo/libjpeg-turbo/releases/download/3.1.4.1/` → `libjpeg-turbo-3.1.4.1-vc-x64.exe` (`bin/turbojpeg.dll`), `-vc-arm64.exe`, `libjpeg-turbo-official_3.1.4.1_amd64.deb` / `_arm64.deb` (`data.tar.xz!./opt/libjpeg-turbo/lib64/libturbojpeg.so.0.4.0`), `libjpeg-turbo-3.1.4.1.dmg` (universal `libturbojpeg.0.4.0.dylib`). JNI exports identical to the owner's 3.0.91 DLL.
- 3.0.4 official: same asset kinds, 34 JNI exports (older names `loadImage`, no ICC) — compatible only with 3.0.x Java; no Windows arm64 build. Not used.
- **3.2.0 official: NO JNI** (`turbojpeg-jna.jar` sample; the Java API moved to `https://github.com/libjpeg-turbo/turbojpeg-java`). Do not use with the vendored Java.
- Owner's `turbojpeg.dll` (VS2015 build of 3.0.91) in `xanderView/lib/jpegturbo_win64.7z`, not used.

## webp-imageio (vendored Java `com.luciad.imageio.webp`; 76 exports)
- **`io.github.darkxanter:webp-imageio:0.3.3` (used, 5 platforms):** Central jar members `native/{Linux/x86_64,Linux/aarch64,Mac/x86_64,Mac/aarch64,Windows/x86_64}/…`; raw files also at `https://raw.githubusercontent.com/darkxanter/webp-imageio/0.3.3/src/main/resources/native/<OS>/<arch>/…`. Byte-identical to the owner's set (`xanderView/lib/libwebp-imageio.7z`, which additionally has Linux x86/arm/armv6/armv7/ppc64 and Windows x86).
- `com.github.usefulness:webp-imageio:0.11.0` (libwebp 1.6.0): same 5 platforms but a changed JNI signature set (boolean getters) — needs its Java too.
- `org.sejda.imageio:webp-imageio:0.1.6`: old `native/linux/64` layout, no arm64. `com.github.gotson:webp-imageio:0.2.2`: no Mac arm64.
- **Windows arm64: none for the vendored `com.luciad` JNI binding.** Alternatives (owner's list, 2026-10-13):
  - Pure-Java decoders (no natives, any platform; decode only):
    - `https://github.com/NostrGameEngine/image-webp-decoder` — `org.ngengine:image-webp-decoder`, MIT, Java 8+, own API (`WebPDecoder.decode` → RGBA `ByteBuffer`), still images only; a mostly machine-translated port of the Rust `image-webp` crate (README warns about bugs), no releases yet.
    - `https://github.com/umjammer/vavi-image-webp` — pure-Java VP8 (lossy) ImageIO SPI (from javavp8decoder), Java 17, jitpack.
    - `https://github.com/haraldk/TwelveMonkeys` — `com.twelvemonkeys.imageio:imageio-webp:3.15.0`, BSD-3-Clause, Central, pure-Java **read-only** ImageIO plugin (VP8, VP8L, alpha).
  - Native-backed libraries with WebP support:
    - **`https://github.com/MrNanko/webp4j` — `dev.matrixlab.webp4j:webp4j-core:2.5.0`, MIT, JNI on libwebp 1.6.0, natives for Windows x64 **and arm64**, macOS x64/arm64, Linux x64/arm64; own `WebPCodec` API (decode, encode, animated WebP, GIF→WebP), not an ImageIO plugin. Modern and fast; the owner already uses it in other projects → the candidate to **replace** the vendored luciad binding on all 6 platforms. To fit the design its natives must be loaded through `LibraryLoader` (vendor/patch its loader or ship its DLLs/.so/.dylib in the bundles), and a small ImageIO SPI or a direct `ImageLoader` path is needed.**
    - `https://github.com/nkolosnjaji/java-imageio-webp` — `io.github.nkolosnjaji:java-imageio-webp` (+ `-lib` natives with arch classifier), BSD-3-Clause, FFM (Java 25+), ImageIO plugin, read+write, libwebp 1.6.0; natives only for Linux x86-64 and macOS x64/arm64 (no Windows).
    - `https://github.com/usefulness/webp-imageio` — `com.github.usefulness:webp-imageio:0.11.0`, Apache-2.0, JNI, libwebp 1.6.0, `com.luciad.imageio.webp` package with changed JNI signatures; Linux x64/arm64, macOS x64/arm64, Windows x64 — no Windows arm64.

## libavif (vendored JNA binding from 1.0.3 headers; SONAME 16 through 1.4)
- **Windows x64 (used):** SDL3_image 3.4.8 `https://github.com/libsdl-org/SDL_image/releases/download/release-3.4.8/SDL3_image-3.4.8-win32-x64.zip` → `optional/libavif-16.dll` (libavif 1.1.1, static dav1d + aom, imports KERNEL32 + msvcrt). Renamed to `libavif.dll`.
- **Windows arm64 (used):** `…/SDL3_image-3.4.8-win32-arm64.zip` → `optional/libavif-16.dll` (1.1.1, imports `aom.dll` + `dav1d.dll`) + `optional/aom.dll` (libaom 3.6.1) + `optional/dav1d.dll`; all ARM64, KERNEL32 only.
- **Linux x86_64 / aarch64 (used):** pillow-avif-plugin 1.6.0 manylinux2014 wheels → `pillow_avif_plugin.libs/libavif-<hash>.so.16.4.2` (libavif 1.4.2, static codecs, glibc 2.14/2.17, libstdc++). Renamed to `libavif.so`.
- **macOS x86_64 / arm64 (used):** pillow-avif-plugin 1.6.0 wheels (`macosx_10_13_x86_64`, `macosx_11_0_arm64`) → `pillow_avif/.dylibs/libavif.16.4.2.dylib` (libSystem only). pillow-avif-plugin 1.4.6 embeds libavif inside the Python extension instead (unusable).
- Rejected: official libavif releases (CLI tools only); Debian `libavif16` (dynamic closure: aom, dav1d, gav1, rav1e, SvtAv1Enc, yuv, abseil, jpeg62); Homebrew bottles (aom, dav1d, libvmaf siblings with absolute install names); MSYS2 (`libavif-16.dll` + 6 codec/runtime DLLs); conda-forge (siblings; no win-arm64).
- Owner's `libavif.dll` (MinGW 1.1.1, static) not used; same version as SDL3's.

## libjxl `jxl_jni` (vendored `org.jpeg.jpegxl.wrapper`, 2 exports)
- **Windows x64 (used):** `https://github.com/libjxl/libjxl/releases/download/v0.12.0/jxl-x64-windows-static.7z` → `x64-windows-static/lib/jxl_jni.dll` (self-contained). The dynamic package `jxl-x64-windows.7z` has `lib/jxl_jni.dll` needing `jxl.dll`, `jxl_threads.dll`, `brotlidec.dll`, MSVCP140.
- **Linux (used):** Debian 13 `libjpegxl-java_0.11.2-0.1~deb13u2_{amd64,arm64}.deb` → `usr/lib/<triplet>/jni/libjxl_jni.so` + siblings from `libbrotli1_1.1.0-2+b7` (`libbrotlicommon.so.1`, `libbrotlidec.so.1`), `libhwy1t64_1.2.0-2+b2` (`libhwy.so.1`), `libjxl0.11_0.11.2-0.1~deb13u2` (`libjxl_threads.so.0.11`); glibc 2.35. Also Arch `libjxl` ships `usr/lib/libjxl_jni.so`.
- **macOS: none prebuilt** (Homebrew/MacPorts: JNI off) → `.github/workflows/natives-build.yml` (libjxl v0.12.0, `BUILD_SHARED_LIBS=OFF`, target `jxl_jni`).
- **Windows arm64: none.** Pure-Java candidate: jxlatte (`https://github.com/Traneptora/jxlatte`, MIT, Java 21) via `https://github.com/umjammer/vavi-image-jxlatte` (ImageIO SPI, Java 17): slow, single-threaded, 8-bit output.
- Owner's `libjxl_jni.dll` (custom static) not used; `xanderView/lib/jxl/` holds the dynamic trio.

## OpenCV 4.6.0 (`opencv_java460`)
- WPILib `https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/thirdparty/frc2023/opencv/opencv-jni/4.6.0-5/opencv-jni-4.6.0-5-<classifier>.jar` with classifiers `windowsx86-64`, `windowsarm64`, `linuxx86-64`, `linuxarm64`, `osxx86-64`, `osxarm64` (`osxuniversal`, `windowsx86`, `linuxarm32`, `linuxathena` also exist; `…debug` variants are 10× larger). Used on 5 platforms. Linux needs libstdc++/libatomic (glibc 2.34); Windows needs `CONCRT140.dll` (VC++ redistributable) + Media Foundation.
- **Windows x64 (used):** the owner's custom minimal build (3.4 MB, same imports), re-hosted as release asset `opencv_java460-windows-x86_64-custom.dll`.
- Classes jar: `edu.wpi.first.thirdparty.frc2023.opencv:opencv-java:4.6.0-5` (parent pom). Owner's archive `xanderView/lib/libsopencv.7z.*` also holds 4.8.0-4 jars + natives (would need the matching Java).

## Not needed any more
- `libgcc_s_seh-1.dll` (MinGW runtime): nothing imports it (PE import tables of every DLL); the 7-Zip init no longer requests it.
- lz4-java JNI (`liblz4-java.*`): `Hasher` uses the pure-Java xxHash.

## Still missing (owner adds later)
| platform | library | plan |
|---|---|---|
| windows-aarch64 | `lib7-Zip-JBinding.dll` 16.02-2.01 | owner builds from the Release-16.02-2.01 sources (MinGW/clang arm64); drop into `natives/local` or re-host on the natives release, add the TSV row |
| windows-aarch64 | `webp-imageio.dll` | optional; preferred: switch all platforms to webp4j (JNI, has Windows arm64; natives via `LibraryLoader`); alternatives: pure-Java fallback (TwelveMonkeys `imageio-webp`, vavi-image-webp, NostrGameEngine `image-webp-decoder`) or an own build of darkxanter's `webp-imageio.c` |
| windows-aarch64 | `libjxl_jni.dll` | optional; jxlatte fallback, or `natives-build.yml` extended with a `windows-11-arm` job |
| macos-x86_64, macos-aarch64 | `libjxl_jni.dylib` | `natives-build.yml` (written, run it) |

## 2026-10-13 (evening): owner's fork builds, webp4j, libjpeg-turbo 3.2.x over FFM
- **7-Zip-JBinding 26.03-2.3** (Java = the fork's `jbinding-java/src`, 77 files incl. the owner's `RandomAccessNioStream`): `https://github.com/korvin2000/sevenzipjbinding/releases/download/Release-26.03-2.3-ci3/` → `Windows-amd64-avx2.zip` (a1abb17e…, member `…/lib/sevenzipjbinding-Windows-amd64.jar!Windows-amd64/lib7-Zip-JBinding.dll` cad1dc45…, 2909184), `Windows-arm64.zip` (bbb5fd1c…, DLL 6fecfd07…, 2593792), `Mac-x86_64.zip` (c9c7f32b…, dylib 6f171ede…, 2261016), `Mac-arm64.zip` (2589f7e7…, dylib f1cac9f7…, 2214920). The `Linux-*-musl` zips link `libc.musl-x86_64.so.1` (unusable on glibc); a glibc workflow was added to the fork (ubuntu-22.04 / ubuntu-22.04-arm).
- **libjpeg-turbo 3.2.x** (C API over FFM, no JNI): owner's fork `https://github.com/korvin2000/libjpeg-turbo/releases/download/win64-compilers-20261013-4/libjpeg-turbo-3.2.1-win64-clang-nightly-avx2-lto-pgo.zip` (b87d9fa9…, `bin/libturbojpeg.dll` 911f7b11…, 1111040); official 3.2.0: `vc-arm64.exe` (269fc323…, `bin/turbojpeg.dll` af37ea6e…, 1008128), `amd64.deb` (21297da4…, `libturbojpeg.so.0.5.0` cc9cc06a…, 1263936), `arm64.deb` (4bf51ee5…, 7c58eda9…, 1130928), `.dmg` (e69c4a3b…, universal `libturbojpeg.0.5.0.dylib` ff8a253e…, 2412048).
- **webp4j 2.5.0** JNI library: owner's fork `https://github.com/korvin2000/libwebp/releases/download/natives-win-x64-20261013/webp4j-windows-x64.dll` (077393ee…, 764416, clang avx2-lto-pgo); stock natives in `https://repo1.maven.org/maven2/dev/matrixlab/webp4j/webp4j-core/2.5.0/webp4j-core-2.5.0.jar` (bd546230…): `native/webp4j-windows-aarch64.dll` 088c1048…, `libwebp4j-linux-x64.so` 8d5e623d…, `libwebp4j-linux-aarch64.so` 9b11baff…, `libwebp4j-mac-x64.dylib` 6cdd67fd…, `libwebp4j-mac-arm64.dylib` 54290d33…. The JNI library's `JNI_OnLoad` caches `WebPBitstreamFeatures`, `AnimationInfo`, `AnimatedWebPData` (so those classes must be on the classpath even for decode-only use).
- **Owner's Windows x64 builds** (now in the tree, TSV url `-`): `libjxl_jni.dll` a7a54377…, `libavif.dll` d6dc02fa…, `opencv_java460.dll` 01e27d61….
- **panamage-jxl 0.3.0** (`net.sourceforge.streamsupport:panamage-jxl-*`, libjxl 0.12 over FFM, JDK 22+): natives for windows-x86_64, linux x86_64/aarch64 (glibc + musl), macos-aarch64 only — no Windows arm64, no macOS x86_64; six DLLs per platform; ~20 MB native memory per concurrent encode (owner's measurements in `howto_use.md`). Rejected for the viewer; `jxl_jni` built by our CI covers all six platforms.
- **TurboJPEG binding choice** (JNI vs FFM vs JNA): numbers in docs/DEPENDENCIES.md §4.
- **webp4j natives for all six platforms** (owner's libwebp fork, `https://github.com/korvin2000/libwebp/releases/download/Release-1.6.0-2.5.0-ci1/`, libwebp 1.6.0, JUnit-tested per platform): `webp4j-windows-x64-avx2.dll` 92816db9… (764416, clang-nightly avx2-lto-pgo), `webp4j-windows-aarch64.dll` 80e3c783… (579072), `libwebp4j-linux-x64.so` 36fde901… (866608, glibc 2.34), `libwebp4j-linux-aarch64.so` e95dbcde… (642440), `libwebp4j-mac-x64.dylib` 0b3b0d54… (813672, macOS 10.15+), `libwebp4j-mac-arm64.dylib` 89291f80… (607960). Portable builds: `-O3 -ffast-math -flto`, no `-march`.
- **7-Zip-JBinding glibc builds** (fork release `Release-26.03-2.3-ci4`, workflow `linux-glibc.yml`): `Linux-amd64-avx2` .so 15c3acdf… (5035776), `Linux-arm64` .so c5b9c6a2… (5127800); NEEDED libc/libm/ld-linux only, glibc 2.35, Clang 23.1.2 + lld, static libstdc++/libgcc. ci4 Windows DLLs: amd64-avx2 69d4c5d9…, arm64 2defaca1…; the macOS dylibs are byte-identical to ci3.
- **jxl_jni 0.12.0 for the five non-Windows-x64 platforms** (our CI, `https://github.com/korvin2000/xview/releases/download/natives-jxl-0.12.0/`, static brotli+highway+skcms): `jxl_jni-windows-aarch64.dll` 1174417b… (1208320), `libjxl_jni-linux-x86_64.so` cc668633… (1842464), `libjxl_jni-linux-aarch64.so` 00f1e368… (1875000), `libjxl_jni-macos-x86_64.dylib` fdac49f5… (1514512), `libjxl_jni-macos-aarch64.dylib` 635c0c8a… (951904). The Debian 0.11.2 `libjpegxl-java` rows and their four siblings are retired.
- **2026-10-13 late: the forks' CI releases for all six platforms** (Clang + LTO + PGO, AVX2 variants on x86_64): libjpeg-turbo `https://github.com/korvin2000/libjpeg-turbo/releases/download/Release-3.2.1-ci2/` (`Windows-x64-avx2` zip 8be8a2ba… → `bin/libturbojpeg.dll` 99789a78… 1125888; `Windows-arm64` a2ee5d66… → 52278145… 846336; `Linux-x64-avx2` d597cd42… → `lib/libturbojpeg.so.0.5.0` 63719cfb… 1146600; `Linux-arm64` 4beb9c5c… → 39b223e4… 862008; `macOS-x64-avx2` 5d136e2f… → `lib/libturbojpeg.0.5.0.dylib` 7dbfc990… 1099032; `macOS-arm64` eea302cc… → 7a1c3eee… 846824; glibc 2.35, macOS 11+); webp4j `https://github.com/korvin2000/libwebp/releases/download/Release-1.6.0-2.5.0-ci2/` (`webp4j-windows-x64-avx2.dll` 9e9bdcf7… 764416, `webp4j-windows-aarch64.dll` 8dac6c3f… 496128, `libwebp4j-linux-x64-avx2.so` 67f60c06… 776456, `libwebp4j-linux-aarch64.so` 642f0648… 558568, `libwebp4j-mac-x64-avx2.dylib` 436bf534… 763152, `libwebp4j-mac-arm64.dylib` f101242d… 576976). The ci1 webp4j rows and the official libjpeg-turbo 3.2.0 rows are superseded.
