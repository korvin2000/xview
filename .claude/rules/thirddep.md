---
paths:
  - "thirddep-j7/**"
---
# thirddep-j7: vendored third-party code

- **Why it is vendored:** the owner uses custom, performance-optimized native builds. One loader (`net.j7.commons.jni.LibraryLoader`) copies them **once** into a persistent per-user dir and loads them from there. Stock jars bundle and self-extract their own natives for every OS, which this design replaces. So the loaders of SWT, 7-Zip-JBinding, TurboJPEG, WebP, AVIF and JXL are patched. **Don't "fix" this by switching to stock artifacts** (see `docs/DEPENDENCIES.md` §0).
- **Do not reformat, refactor, "clean up", or modernize it.** Large diffs here hide the real patches.
- Allowed changes:
  - minimal fixes
  - native-loading patches
  - vendoring a tiny missing class (e.g. `vavi.imageio.WrappedImageInputStream`, PLAN P1.3)
  - deleting whole unused packages (PLAN P2.4)

  Mark each patched spot `// xview-patch: <reason>`, and record it in `docs/DEPENDENCIES.md` §2.
- Existing patches route loading to LibraryLoader (load names in parentheses):
  - SWT `internal/Library.loadLibrary` (`swt`; `swt-gdip` and `swt-awt` soft-fail)
  - `SevenZip.ensureLibraryIsInitialized` (`lib7-Zip-JBinding`, `libgcc_s_seh-1`)
  - `TJLoader` (`turbojpeg`)
  - `WebP` (`webp-imageio`)
  - `Avif` (`checkLibrary("libavif", false)` → JNA `avif`)
  - `JpegXLImageReader` (`libjxl_jni`)
- A load name **must equal the file stem** in `libs/<os>/<arch>/`. Renaming a load name requires renaming the DLL in the native source dir **and** in the user's cache.
- The vendored SWT is **4.527, win32 flavor** (Eclipse 4.5). It matches the custom `swt.dll`, which identifies itself as `swt-win32-4527`, and it is the only SWT on the classpath (the 4.3 artifact was removed). Its message bundles are in `src/main/resources/org/eclipse/swt/internal/SWTMessages*.properties`. Never use APIs from newer SWT docs without checking the vendored source. Linux and macOS need gtk/cocoa SWT code (PLAN P5).
- `vavi/imageio/WrappedImageInputStream.java` is vendored from vavi-image 1.0.11, with an `available()` xview-patch. Don't re-add the `vavi-image` dependency (its 1.0.11 jitpack artifact gives 404; newer versions pull `jiu` + `native-lib-loader`).
- When changing ImageIO plugins, keep `src/main/resources/META-INF/services/*` in sync.
- Scope searches to the specific package. The SWT tree alone has about 800 files.
- Compile-check with `bash tools/javac-check.sh` (thirddep compiles with `--add-exports jdk.unsupported/com.sun.nio.file=ALL-UNNAMED` at level 16).
