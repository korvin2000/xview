---
paths:
  - "commons-j7/**"
---
# commons-j7: shared utilities

- Consumers are `thirddep-j7` (patched native loading) and `xanderView`. Before changing any public signature, grep both trees (`net.j7.commons.<Class>`). The used set is in `docs/DEPENDENCIES.md` §3.
- **`jni/LibraryLoader` is the heart of the custom native design.** All 8 natives (Windows: `swt`, `lib7-Zip-JBinding`, `libgcc_s_seh-1`, `turbojpeg`, `webp-imageio`, `libavif`, `libjxl_jni`, `opencv_java460`) go through it, so a regression breaks the app at startup. These are deployment contracts:
  - the source `<location>/libs/<os>/<arch>/<file>`, with `location` from `-Dxview.libs.location` > env `XVIEW_LIBS_LOCATION` > `/libs.properties` > the jar holding it; deployed as `C:\Program Files\xview`
  - the file per load name: Windows exactly `<name>.dll`; Unix also `lib<name>`; macOS `.dylib` then `.jnilib` (JNA only `.dylib`); a source candidate beats a cache-only candidate
  - the cache `<AppDataDir>/libs/<os>/<arch>/`, resolved by `io/SysInfo`; on Windows `%APPDATA%\xanderView`
  - the os dirs (`windows`, `linux`, `macos`) and arch names (`amd64`, `x86_64`, `aarch64`; `arm64` maps to `aarch64`)
  - the copy-on-change (exact content compare, atomic temp publish, rename-old fallback for mapped DLLs, load-from-source fallback), the Windows dependency map (`libavif` → `libgcc_s_seh-1`, copy only) and the soft-fail semantics (`tryLoadLibrary` reports the result for callers that must fail fast, e.g. the stock-SWT `Library`)

  P5.2 and B6/B7/B8 are done; known open bug: B10.
- The dead packages were deleted (PLAN P2.3); some unused classes remain in the kept packages. Don't extend them. Keep `jni`, `io.SysInfo`, `io.FilePath`, `io.helpers.WildcardMatcher`.
- Language level is 21 (`release` from the parent pom). Logging uses SLF4J (`slf4j-api` only; the `slf4j-simple` binding is in the app).
