# Build and run

## Why it is hard to build
- **Native libraries:** tracked in `natives/<os>/<arch>/` (since 2026-10-13), rebuilt from the hash-pinned manifests `natives/<platform>.tsv` by `tools/natives/bundle.sh`; the build takes the tracked tree and falls back to downloading the bundle zip of the GitHub release `natives-<rev>` when the tree is absent. See [NATIVES.md](NATIVES.md), [DEPENDENCIES §0, §4](DEPENDENCIES.md).
- **jitpack-only vavi artifacts** and outdated libraries.
- The small classes-only OpenCV jar is no longer a blocker: it comes from the WPILib Maven repo (PLAN P1.4).

## Toolchain
- **JDK 26+** to build and run (class files v70); every module compiles with `release=26` (migrated from 21 in PLAN P6). The deployed install still bundles **BellSoft Liberica JRE 23** (`C:\Program Files\xview\jre`), which cannot run a new jar: replace it with a 26 runtime before deploying one.
- **Maven:** use the wrapper (`./mvnw`, `mvnw.cmd`). It downloads Maven 3.9.16 once (SHA-256 checked) into `~/.m2/wrapper`. On Unix the wrapper needs `unzip` (otherwise it fetches the `.tar.gz`, which fails the zip checksum). The enforcer rejects JDK < 26 and Maven < 3.9. On the owner's machine IntelliJ also bundles Maven 3.9.16 at `C:\Program Files\JetBrains\IntelliJ IDEA 2023.1\plugins\maven-plugin\lib\maven3\bin\mvn`; JDK 26: `devtools/jdk-26.0.2.1+1` in the repo (untracked; set `JAVA_HOME` to it). IntelliJ 2023.1 predates language level 26; the build itself does not depend on the IDE.
- Platforms: **Windows x64** runs today. Linux, macOS and Windows arm64 dist zips build with their natives (see SWT flavors and natives below) but are unverified at runtime (PLAN P7.5); the Windows arm64 bundle is incomplete (P7.7).

## Build (verified 2026-10-12: BUILD SUCCESS)
```bash
./mvnw -B -DskipTests package
```
```bash
./mvnw -B -pl xanderView -am compile
```
- `package` builds all modules. `xanderView/target/xanderview-j7-1.0.0.jar` is the shaded, runnable app jar; it is the only shaded jar (commons and thirddep are plain library jars).
- `-pl xanderView -am compile` builds only the app and its upstream modules.
- Dependencies: Central, jitpack (vavi-commons) and `https://frcmaven.wpi.edu/artifactory/release/` (only for the OpenCV jar `edu.wpi.first.thirdparty.frc2023.opencv:opencv-java:4.6.0-5`, byte-identical to the former local install, SHA-256 `dd74cfd4…697b6`; `checksumPolicy=fail`).
- Avoid `mvn clean` unless you mean it: `xanderView/target/` also holds the owner's copies (`xanderview.jar`, `*.jar.ok`).
- Expected noise: one shade "overlapping resource" warning for `META-INF/MANIFEST.MF` (benign), and deprecation warnings from vendored code.
- Building does not touch the deployed `C:\Program Files\xview\xanderview.jar`. Deploying means copying the new jar there by hand.

### SWT flavors (PLAN P5.1)
The parent pom has one `swt-*` profile per platform. It is selected from the build machine's OS/arch, or forced with `-Dswt.flavor`:

| `-Dswt.flavor=` | SWT Java code | Output dir |
|---|---|---|
| `win32.win32.x86_64` (auto on Windows x64) | stock SWT 3.135.0 classes + patched `Library` (`thirddep-j7/src/swt-stock/`), since P7 | `target/` |
| `win32.win32.aarch64` (auto on Windows arm64) | same; natives complete since 2026-10-13 (PLAN P7.7) | `target-<flavor>/` |
| `gtk.linux.x86_64`, `gtk.linux.aarch64` | same | `target-<flavor>/` |
| `cocoa.macosx.x86_64`, `cocoa.macosx.aarch64` | same | `target-<flavor>/` |

```bash
./mvnw -B -DskipTests -Dswt.flavor=gtk.linux.x86_64 package
```
- For every flavor, thirddep unpacks `org.eclipse.platform:org.eclipse.swt.<ws>.<os>.<arch>:3.135.0` at build time into its classes **without** the natives, the signature files and the stock `org.eclipse.swt.internal.Library`. The patched `Library` loads every SWT native through `LibraryLoader`, so nothing is extracted from the jar.
- Each non-Windows flavor builds into its own `target-<flavor>/`, so flavors never mix classes. IDE builds of a stock flavor need one Maven build first (the unpack step).
- An unsupported OS/arch fails the enforcer with a hint to pass `-Dswt.flavor`.
- All flavors share the Maven coordinates of `thirddep-j7` and the app, so the non-Windows flavors skip `install`/`deploy`: `~/.m2` only ever holds the Windows flavor. Build a cross flavor in **one reactor run from the root** (or with `-am`); `-Dswt.flavor=… -pl xanderView` without `-am` would shade the installed Windows thirddep (PLAN B21).
- Cross-building on Windows was verified for `gtk.linux.x86_64` and `cocoa.macosx.aarch64`: the app compiles against SWT 3.135 and the jars contain no SWT natives.

### Natives bundles and dist zips (PLAN P7)
Each `swt-*` profile also sets `xview.platform` (`windows-x86_64`, `windows-aarch64`, `linux-x86_64`, `linux-aarch64`, `macos-x86_64`, `macos-aarch64`), `xview.libdir` (`windows/amd64`, `linux/x86_64`, …) and the bundle hash `xview.natives.sha256`. At `initialize` the app module (antrun) picks the natives directory `xview.natives.libs`: the tracked tree `natives/<os>/<arch>/` when it holds `natives.sha256`, else `-Dxview.natives.dir=<dir containing libs/>`, else `target/natives/<sha256>/libs/<os>/<arch>/` filled in `generate-resources` by downloading `xview-natives-<platform>-<rev>.zip` from the GitHub release `natives-${xview.natives.rev}` (`download-maven-plugin`, SHA-256 verified, cached in `~/.m2`). `process-resources` packs that directory's `natives.sha256` + `natives.deps` into the jar, and after shading **`xanderview-<version>-<platform>.zip`** is built (`maven-assembly-plugin`, `src/assembly/dist.xml`): `xanderview.jar`, `libs/<os>/<arch>/…`, `xanderview.cmd` or `xanderview.sh`, a filtered `jrunner.json`, `THIRD-PARTY-NOTICES.md`.
- The default build uses the tracked tree (no download; the Maven log says `natives: …/natives/windows/amd64 (download skipped: true)`). Without the tree (e.g. a sparse checkout) it downloads the bundle (release `natives-${xview.natives.rev}`); the download is cached in `~/.m2/repository/.cache/download-maven-plugin`. `-Dxview.natives.skip=true` builds without natives as before P7 (jar without a manifest, legacy loader mode, no dist zip).
- Another tree instead of the tracked one: `-Dxview.natives.dir=<absolute dir that contains libs/<os>/<arch>/>` (e.g. an unpacked bundle zip). Rebuilding the tracked tree and its zip from the manifests:
```bash
tools/natives/bundle.sh windows-x86_64 --rev 2026.10.1
```
```bash
./mvnw -B -DskipTests package
```
```bash
./mvnw -B -DskipTests -Dswt.flavor=gtk.linux.x86_64 package
```
- Verified 2026-10-13 on Windows: the tree build, the download fallback (tree hidden) and the cross-build succeed; the zips contain the natives, the manifests and the launcher; the unpacked Windows dist passes `--check-natives` and decodes JPEG (FFM), WebP (webp4j) and extracts zip/7z (7-Zip 26.03).
- Check a dist: unzip it and run `java -jar xanderview.jar --check-natives` (loads every native of the manifest from a cold per-user cache, exit 1 on a problem). CI does this per platform (`.github/workflows/build.yml`); the bundler runs in `natives-bundle.yml`.
- `tools/natives/check-binaries.sh natives/<os>/<arch>` (needs LLVM tools) lists imports, glibc floor, macOS minos and JNI export counts.

Jar acceptance checks after a build:
- **Manifest:** `Main-Class`, `SWT-OS/WS/Arch` of the flavor, `Enable-Native-Access: ALL-UNNAMED` (no `Add-Exports` since P7).
- **`META-INF/services`:** `ImageReaderSpi` lists AVIF and JXL-JNI; `ImageWriterSpi` lists AVIF (WebP goes through webp4j directly, no SPI).
- **Entries present:** `vavi/imageio/WrappedImageInputStream.class`, `org/eclipse/swt/internal/Library.class` (the patched one: `javap -c` shows `LibraryLoader` calls), `libs.properties`; Windows also `org/eclipse/swt/internal/SWTMessages*.properties`; with a natives bundle also `libs/<os>/<arch>/natives.sha256`.
- **No SWT or JXL natives** (`*.dll`, `*.so`, `*.jnilib`, `*.dylib` outside `com/sun/jna/` and lz4's own dirs, which are the documented exceptions in [DEPENDENCIES §4](DEPENDENCIES.md#natives)).

## Fast compile check without Maven
```bash
bash tools/javac-check.sh
```
It compiles the main sources of commons → thirddep (Windows flavor: the stock SWT 3.135 win32 jar from `~/.m2` + the patched `Library`) → app with `javac --release <maven.compiler.release of the parent pom>` (a JDK of that version: `JAVA_HOME`, `devtools/jdk-<rel>*`, `~/.jdks/*-<rel>*` or `PATH`) into a disposable dir (`${TMPDIR:-/tmp}/xview-javac`, or the given path) in about 30 s, using jars from `~/.m2` (run one Maven build first). It refuses to clear a non-empty directory it didn't create. It doesn't cover resources, tests, shading, natives or the stock SWT flavors, so it is not release acceptance.

## Pom notes
- All modules: `maven.compiler.release=26` from the parent; plugin versions pinned in the parent `pluginManagement`; the enforcer runs in every module.
- No `--add-exports` anywhere since P7: `com.sun.nio.file` is exported by `jdk.unsupported`, and stock SWT 3.135 creates the embedded AWT frame through JNI on every platform (the deleted vendored 4.527 reflected into `sun.awt.windows`).
- The vavi dependencies: only `com.github.umjammer.vavi-commons:vavi-commons:1.1.14` (jitpack) remains.
- Logging: `slf4j-api` everywhere, the `slf4j-simple` binding only in the app.

## Deploy layout (owner's install)
```
C:\Program Files\xview\
├─ xanderview.jar            ← copy of xanderView/target/xanderview-j7-1.0.0.jar
├─ libs\windows\amd64\*.dll  ← native SOURCE, legacy mode (no natives.sha256); configured today via -Dxview.libs.location or XVIEW_LIBS_LOCATION, or by placing the jar next to libs\ (default location since P7)
├─ jre\                      ← Liberica JRE 23
└─ imageViewer.cmd, start23.bat (launchers)
%APPDATA%\xanderView\libs\windows\amd64\  ← native CACHE
```
- Legacy mode (no `natives.sha256`): the cache is refreshed automatically when a source DLL's content differs, by exact comparison on first use per JVM. Dist zips (manifest mode) use `%APPDATA%\xanderView\libs\windows\amd64\<bundle12>\` instead ([NATIVES.md §4](NATIVES.md)).
- If a DLL is still loaded by another running instance, the old file is renamed to `*.old-<n>` and swept later.
- To update a native, replace it in `libs\windows\amd64\` and restart. No manual cache deletion is needed (since the B8 fix).
- Another natives source can be chosen without rebuilding: `-Dxview.libs.location=<dir or URL>` or the environment variable `XVIEW_LIBS_LOCATION` (both override `libs.properties`).

## Run
```bash
java -jar xanderView/target/xanderview-j7-1.0.0.jar "D:\pics\holiday"
```
- No JVM flags are needed with `java -jar` (manifest `Enable-Native-Access`); `--add-exports` is not needed any more (P7).
- From the IDE (classpath launch): main class `org.delafer.xanderView.gui.MainEntryPoint`; add `--enable-native-access=ALL-UNNAMED` to the VM options to silence the JNI warnings. Natives: `-Dxview.libs.location=<dir containing libs/>` (a bundle such as `xanderView/target/natives/<sha256>`, or the deployed `C:\Program Files\xview`); without it a classpath launch is cache-only.
- Linux/macOS: `PlatformLaunch` restarts the app once when needed: with `-XstartOnFirstThread` on macOS, and with `GDK_BACKEND=x11` in a Wayland session on Linux (`SWT_AWT` needs X11). Disable it with `-Dxview.relaunch=false` if your launcher already sets these.
- Logging: slf4j-simple to stderr at `info`; more detail with `-Dorg.slf4j.simpleLogger.defaultLogLevel=debug` (see `xanderView/src/main/resources/simplelogger.properties`).
- Prerequisites:
  - The natives must be reachable: `libs/<os>/<arch>/` next to the jar (dist zip), `-Dxview.libs.location`/`XVIEW_LIBS_LOCATION`, or an already filled cache ([DEPENDENCIES §4](DEPENDENCIES.md#natives)). `java -jar xanderview.jar --check-natives` tells which ones load.
  - OpenCV needs the MSVC 2015–2022 x64 runtime.
- Optional config: `%APPDATA%\xanderView\xander.properties` (sample in `xanderView/src/main/resources/xander.properties`). Set `target.folder` and `cry.keyfile`; the defaults point to `A:\`.

## Tests
There are none yet: 0 `@Test` (PLAN P3.1, deferred). The headless native probe used on 2026-10-12 (isolated `APPDATA`, cold cache, decode/resize/archive per native) lives in a scratch dir and is not committed. After JUnit 5 is added, `./mvnw -pl xanderView test -Dtest=ClassName#method` runs a single test.
