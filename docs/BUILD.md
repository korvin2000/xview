# Build and run

## Why it is hard to build
- **Custom native libraries:** some are built and copied by hand, they are not in git, and they are loaded through the patched vendored libraries. See [DEPENDENCIES §0, §4](DEPENDENCIES.md).
- **jitpack-only vavi artifacts** and outdated libraries.
- The small classes-only OpenCV jar is no longer a blocker: it comes from the WPILib Maven repo (PLAN P1.4).

## Toolchain
- **JDK 21+** to build and run; every module compiles with `release=21`. The deployed install bundles **BellSoft Liberica JRE 23** (`C:\Program Files\xview\jre`).
- **Maven:** use the wrapper (`./mvnw`, `mvnw.cmd`). It downloads Maven 3.9.16 once (SHA-256 checked) into `~/.m2/wrapper`. On Unix the wrapper needs `unzip` (otherwise it fetches the `.tar.gz`, which fails the zip checksum). The enforcer rejects JDK < 21 and Maven < 3.9. On the owner's machine IntelliJ also bundles Maven 3.9.16 at `C:\Program Files\JetBrains\IntelliJ IDEA 2023.1\plugins\maven-plugin\lib\maven3\bin\mvn`; JDKs live under `~/.jdks/` (use `temurin-21.0.9`).
- Platforms: only **Windows x64** runs today. Linux and macOS jars build (see SWT flavors below) but have no natives yet (PLAN P5.3).

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
| `win32.win32.x86_64` (auto on Windows x64) | vendored 4.527 sources, `thirddep-j7/src/swt-win32/` | `target/` |
| `gtk.linux.x86_64`, `gtk.linux.aarch64` | stock SWT 3.135.0 classes + patched `Library` (`thirddep-j7/src/swt-stock/`) | `target-<flavor>/` |
| `cocoa.macosx.x86_64`, `cocoa.macosx.aarch64` | same | `target-<flavor>/` |

```bash
./mvnw -B -DskipTests -Dswt.flavor=gtk.linux.x86_64 package
```
- For the stock flavors, thirddep unpacks `org.eclipse.platform:org.eclipse.swt.<ws>.<os>.<arch>:3.135.0` at build time into its classes **without** the natives, the signature files and the stock `org.eclipse.swt.internal.Library`. The patched `Library` loads every SWT native through `LibraryLoader`, so nothing is extracted from the jar.
- Each non-Windows flavor builds into its own `target-<flavor>/`, so flavors never mix classes. IDE builds of a stock flavor need one Maven build first (the unpack step).
- An unsupported OS/arch fails the enforcer with a hint to pass `-Dswt.flavor`.
- All flavors share the Maven coordinates of `thirddep-j7` and the app, so the non-Windows flavors skip `install`/`deploy`: `~/.m2` only ever holds the Windows flavor. Build a cross flavor in **one reactor run from the root** (or with `-am`); `-Dswt.flavor=… -pl xanderView` without `-am` would shade the installed Windows thirddep (PLAN B21).
- Cross-building on Windows was verified for `gtk.linux.x86_64` and `cocoa.macosx.aarch64`: the app compiles against SWT 3.135 and the jars contain no SWT natives.

Jar acceptance checks after a build:
- **Manifest:** `Main-Class`, `SWT-OS/WS/Arch` of the flavor, `Add-Exports: java.desktop/sun.awt.windows`, `Enable-Native-Access: ALL-UNNAMED`.
- **`META-INF/services`:** `ImageReaderSpi` lists AVIF, WebP and JXL-JNI; `ImageWriterSpi` lists AVIF and WebP.
- **Entries present:** `vavi/imageio/WrappedImageInputStream.class`, `org/eclipse/swt/internal/Library.class` (the patched one: `javap -c` shows `LibraryLoader` calls), `libs.properties`; Windows also `org/eclipse/swt/internal/SWTMessages*.properties`.
- **No SWT or JXL natives** (`*.dll`, `*.so`, `*.jnilib`, `*.dylib` outside `com/sun/jna/` and lz4's own dirs, which are the documented exceptions in [DEPENDENCIES §4](DEPENDENCIES.md#natives)).

## Fast compile check without Maven
```bash
bash tools/javac-check.sh
```
It compiles the main sources of commons → thirddep (Windows SWT flavor) → app with `javac --release 21` into a disposable dir (`${TMPDIR:-/tmp}/xview-javac`, or the given path) in about 30 s, using jars from `~/.m2` (run one Maven build first). It refuses to clear a non-empty directory it didn't create. It doesn't cover resources, tests, shading, natives or the stock SWT flavors, so it is not release acceptance.

## Pom notes
- All modules: `maven.compiler.release=21` from the parent; plugin versions pinned in the parent `pluginManagement`; the enforcer runs in every module.
- No compile-time `--add-exports` are needed (`com.sun.nio.file` is exported by `jdk.unsupported`). The **runtime** export `java.desktop/sun.awt.windows` is required on Windows, because SWT 4.527 `SWT_AWT` reflects into `WEmbeddedFrame`/`WComponentPeer`; it is in the app manifest (`Add-Exports`), which `java -jar` honors. SWT 3.135 gtk/cocoa create the embedded frame through JNI and need no exports.
- The vavi dependencies: only `com.github.umjammer.vavi-commons:vavi-commons:1.1.14` (jitpack) remains.
- Logging: `slf4j-api` everywhere, the `slf4j-simple` binding only in the app.

## Deploy layout (owner's install)
```
C:\Program Files\xview\
├─ xanderview.jar            ← copy of xanderView/target/xanderview-j7-1.0.0.jar
├─ libs\windows\amd64\*.dll  ← native SOURCE (libs.properties location=C:/Program Files/xview)
├─ jre\                      ← Liberica JRE 23
└─ imageViewer.cmd, start23.bat (launchers)
%APPDATA%\xanderView\libs\windows\amd64\  ← native CACHE
```
- The cache is refreshed automatically when a source DLL's content differs, by exact comparison on first use per JVM.
- If a DLL is still loaded by another running instance, the old file is renamed to `*.old-<n>` and swept later.
- To update a native, replace it in `libs\windows\amd64\` and restart. No manual cache deletion is needed (since the B8 fix).
- Another natives source can be chosen without rebuilding: `-Dxview.libs.location=<dir or URL>` or the environment variable `XVIEW_LIBS_LOCATION` (both override `libs.properties`).

## Run
```bash
java -jar xanderView/target/xanderview-j7-1.0.0.jar "D:\pics\holiday"
```
- No JVM flags are needed with `java -jar` (manifest `Add-Exports` and `Enable-Native-Access`). The old form with `--add-exports=java.desktop/sun.awt.windows=ALL-UNNAMED` still works.
- From the IDE (classpath launch, the manifest doesn't apply): main class `org.delafer.xanderView.gui.MainEntryPoint`, VM options `--add-exports=java.desktop/sun.awt.windows=ALL-UNNAMED --enable-native-access=ALL-UNNAMED`.
- Linux/macOS: `PlatformLaunch` restarts the app once when needed: with `-XstartOnFirstThread` on macOS, and with `GDK_BACKEND=x11` in a Wayland session on Linux (`SWT_AWT` needs X11). Disable it with `-Dxview.relaunch=false` if your launcher already sets these.
- Logging: slf4j-simple to stderr at `info`; more detail with `-Dorg.slf4j.simpleLogger.defaultLogLevel=debug` (see `xanderView/src/main/resources/simplelogger.properties`).
- Prerequisites:
  - The native source dir must be reachable (`libs.properties` `location`, or the overrides above), or the cache must already hold the 8 DLLs ([DEPENDENCIES §4](DEPENDENCIES.md#natives)).
  - OpenCV needs the MSVC 2015–2022 x64 runtime.
- Optional config: `%APPDATA%\xanderView\xander.properties` (sample in `xanderView/src/main/resources/xander.properties`). Set `target.folder` and `cry.keyfile`; the defaults point to `A:\`.

## Tests
There are none yet: 0 `@Test` (PLAN P3.1, deferred). The headless native probe used on 2026-10-12 (isolated `APPDATA`, cold cache, decode/resize/archive per native) lives in a scratch dir and is not committed. After JUnit 5 is added, `./mvnw -pl xanderView test -Dtest=ClassName#method` runs a single test.
