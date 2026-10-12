# Build and run

## Why it is hard to build
- **Custom native libraries:** some are built and copied by hand, they are not in git, and they are loaded through the patched vendored libraries. See [DEPENDENCIES §0, §4](DEPENDENCIES.md).
- **A small classes-only OpenCV jar** (WPILib build) that must be installed into `~/.m2` by hand.
- **jitpack-only vavi artifacts** and outdated libraries.

## Toolchain
- **JDK 21+** to build: the app targets 21, and commons and thirddep target 16. The deployed install bundles **BellSoft Liberica JRE 23** (`C:\Program Files\xview\jre`).
- **Maven 3.9+.** There is no wrapper yet (PLAN P1.5). On the owner's machine Maven isn't on PATH, but IntelliJ bundles **Maven 3.9.16** at `C:\Program Files\JetBrains\IntelliJ IDEA 2023.1\plugins\maven-plugin\lib\maven3\bin\mvn`. JDKs live under `~/.jdks/` (use `temurin-21.0.9`).
- Platforms: only **Windows x64** works today. Linux and macOS x86_64 (plus optional aarch64) are targets; see [DEPENDENCIES §4](DEPENDENCIES.md#natives) and PLAN P5.

## One-time setup: the custom OpenCV jar
Either run the owner's script (`xanderView/opencv/register.cmd`, from inside `xanderView/opencv/`) or run this from the repo root:
```bash
mvn install:install-file -Dfile=xanderView/opencv/opencv-460.jar -DgroupId=org.opencv -DartifactId=opencv -Dversion=4.6.0 -Dpackaging=jar
```
The jar is untracked: 326,063 B, SHA-256 `dd74cfd431ef56eb873ce8f81966c4351cbf7f865b798077a2b060787ac697b6`. Without this install, thirddep fails on `org.opencv:opencv:jar:4.6.0`.

## Build (verified 2026-10-12: BUILD SUCCESS)
```bash
mvn -B -DskipTests package
```
```bash
mvn -B -pl xanderView -am compile
```
- `package` builds all modules. `xanderView/target/xanderview-j7-1.0.0.jar` is the shaded, runnable app jar.
- `-pl xanderView -am compile` builds only the app and its upstream modules.
- Avoid `mvn clean` unless you mean it: `xanderView/target/` also holds the owner's copies (`xanderview.jar`, `*.jar.ok`).
- Expected noise: nested shading (all three modules shade) gives "overlapping classes" warnings (PLAN P1.7). `slf4j-simple` and `logback` are a double binding (P1.8).
- Building does not touch the deployed `C:\Program Files\xview\xanderview.jar`. Deploying means copying the new jar there by hand.

Jar acceptance checks after a build:
- **Manifest:** `Main-Class` and `SWT-OS/WS/Arch`.
- **`META-INF/services`:** `ImageReaderSpi` lists AVIF, WebP and JXL-JNI, plus the commented-out JXL-JNA line. `ImageWriterSpi` lists AVIF and WebP.
- **Entries present:** `vavi/imageio/WrappedImageInputStream.class`, `org/eclipse/swt/internal/SWTMessages*.properties`, `libs.properties`.

## Fast compile check without Maven
```bash
bash tools/javac-check.sh
```
It compiles the main sources of commons → thirddep → app with javac into a disposable dir (`${TMPDIR:-/tmp}/xview-javac`, or the given path) in about 30 s, using jars from `~/.m2`. It needs no workarounds anymore. It refuses to clear a non-empty directory it didn't create. It doesn't cover resources, tests, shading or natives, so it is not release acceptance.

## Pom notes
- The vavi dependencies: only `com.github.umjammer.vavi-commons:vavi-commons:1.1.14` (jitpack) remains. `vavi-image` was replaced by the vendored `WrappedImageInputStream`.
- SWT: only the vendored 4.527 sources. The 4.3 artifact was removed.
- `maven.compiler.release` can't simply replace `source`/`target`, because javac rejects `--add-exports` together with `--release` (verified).
  - The **compile-time** exports flags are unnecessary.
  - The **runtime** export `java.desktop/sun.awt.windows` **is required**, because `SWT_AWT` reflects into `WEmbeddedFrame`/`WComponentPeer` (PLAN P1.6: move it into the manifest as `Add-Exports`).
- `commons-j7` shades with a non-existent `Main-Class` (P1.7).

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

## Run
```bash
java --add-exports=java.desktop/sun.awt.windows=ALL-UNNAMED -jar xanderView/target/xanderview-j7-1.0.0.jar "D:\pics\holiday"
```
- From the IDE: main class `org.delafer.xanderView.gui.MainEntryPoint`, VM option `--add-exports=java.desktop/sun.awt.windows=ALL-UNNAMED`. Add `--enable-native-access=ALL-UNNAMED` to silence the JDK 21+ restricted-method warnings.
- Prerequisites:
  - The native source dir must be reachable (or `location` in `commons-j7/src/main/resources/libs.properties` points elsewhere), or the cache must already hold the 8 DLLs ([DEPENDENCIES §4](DEPENDENCIES.md#natives)).
  - OpenCV needs the MSVC 2015–2022 x64 runtime.
- Optional config: `%APPDATA%\xanderView\xander.properties` (sample in `xanderView/src/main/resources/xander.properties`). Set `target.folder` and `cry.keyfile`; the defaults point to `A:\`.
- The repo copy `xanderView/src/test/resources/imageViewer.cmd` is obsolete: `-Xincgc` and `-client` were removed in Java 9+.

## Tests
There are none yet: 0 `@Test`. The headless native probe used on 2026-10-12 (isolated `APPDATA`, cold cache, decode/resize/archive per native) lived in a scratch dir and was not committed. PLAN P3.1 should turn it into JUnit plus a manual smoke test. After JUnit 5 is added, `mvn -pl xanderView test -Dtest=ClassName#method` runs a single test.
