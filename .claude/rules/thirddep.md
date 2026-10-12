---
paths:
  - "thirddep-j7/**"
---
# thirddep-j7: vendored third-party code

- **Why it is vendored:** one loader (`net.j7.commons.jni.LibraryLoader`) copies the natives **once** into a persistent per-user dir and loads them from there; since P7 the natives are upstream builds pinned in `natives/<platform>.tsv` (hand-built overrides remain possible). Stock jars bundle and self-extract their own natives for every OS, which this design replaces. So the loaders of SWT, 7-Zip-JBinding, TurboJPEG, WebP, AVIF and JXL are patched. **Don't "fix" this by switching to stock artifacts** (see `docs/DEPENDENCIES.md` §0).
- **Do not reformat, refactor, "clean up", or modernize it.** Large diffs here hide the real patches.
- Allowed changes:
  - minimal fixes
  - native-loading patches
  - vendoring a tiny missing class (e.g. `vavi.imageio.WrappedImageInputStream`, PLAN P1.3)
  - deleting whole unused packages (PLAN P2.4)

  Mark each patched spot `// xview-patch: <reason>`, and record it in `docs/DEPENDENCIES.md` §2.
- Existing patches route loading to LibraryLoader (load names in parentheses):
  - SWT, all flavors: `src/swt-stock/.../internal/Library.loadLibrary` (upstream 3.135 stems via `LibraryLoader.tryLoadLibrary`; throws if none loads)
  - `SevenZip.ensureLibraryIsInitialized` (`lib7-Zip-JBinding`; the `libgcc_s_seh-1` request was removed in P7)
  - `TJLoader` (`turbojpeg`, direct `LibraryLoader` call)
  - `WebP` (`webp-imageio`)
  - `Avif` (`checkLibrary("libavif", false)` → JNA `avif`)
  - `JpegXLImageReader` (`libjxl_jni`)
- A load name **must equal the file stem** in `libs/<os>/<arch>/` (on Linux/macOS `LibraryLoader` also tries `lib<name>`, and `.dylib`/`.jnilib` on macOS). Renaming a load name requires renaming the DLL in the native source dir **and** in the user's cache.
- **SWT flavors** (parent `swt-*` profiles, `-Dswt.flavor=<ws>.<os>.<arch>`): every flavor, Windows included since P7, uses the stock SWT 3.135 classes, unpacked at build time without natives/signatures/stock `Library`, plus the patched `src/swt-stock/java/org/eclipse/swt/internal/Library.java`. The app must use public SWT API only. The vendored 4.527 win32 sources were deleted in P7 (git history has them); the Windows natives are the stock `swt-*-win32-4974r10.dll` files from the Central jar.
- `vavi/imageio/WrappedImageInputStream.java` is vendored from vavi-image 1.0.11, with an `available()` xview-patch. Don't re-add the `vavi-image` dependency (its 1.0.11 jitpack artifact gives 404; newer versions pull `jiu` + `native-lib-loader`).
- When changing ImageIO plugins, keep `src/main/resources/META-INF/services/*` in sync.
- Scope searches to the specific package.
- Compile-check with `bash tools/javac-check.sh` (release from the parent pom; the Windows flavor is compiled against the stock 3.135 win32 jar from `~/.m2`); other flavors: `./mvnw -B -DskipTests -Dswt.flavor=gtk.linux.x86_64 package`.
