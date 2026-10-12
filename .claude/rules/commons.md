---
paths:
  - "commons-j7/**"
---
# commons-j7: shared utilities

- Consumers are `thirddep-j7` (patched native loading) and `xanderView`. Before changing any public signature, grep both trees (`net.j7.commons.<Class>`). The used set is in `docs/DEPENDENCIES.md` §3.
- **`jni/LibraryLoader` is the heart of the custom native design.** All 8 natives (Windows: `swt`, `lib7-Zip-JBinding`, `libgcc_s_seh-1`, `turbojpeg`, `webp-imageio`, `libavif`, `libjxl_jni`, `opencv_java460`) go through it, so a regression breaks the app at startup. These are deployment contracts:
  - the source `<location>/libs/<os>/<arch>/<name>.<ext>`, with `location` from `/libs.properties`; deployed as `C:\Program Files\xview`
  - the cache `<AppDataDir>/libs/<os>/<arch>/`, resolved by `io/SysInfo`; on Windows `%APPDATA%\xanderView`
  - the os dirs (`windows`, `linux`, `macos`) and arch names (`amd64`, `x86_64`, `aarch64`)
  - the copy-on-change (exact content compare, atomic temp publish, rename-old fallback for mapped DLLs, load-from-source fallback), the Windows dependency map (`libavif` → `libgcc_s_seh-1`, copy only) and the soft-fail semantics

  The cross-platform fixes (`.dylib`, `lib` prefixes, aarch64) are PLAN P5.2. B7 and B8 are fixed; known open bugs: B6, B10.
- About 70% of this module is unused (`logger`, `net`, `reflection`, `ui`, `xml`, `io.zip`, `io.helpers`, the apache/osgi copies). Don't extend dead packages. Removal is tracked in PLAN P2.3; keep `jni`, `io.SysInfo` and `io.FilePath`.
- Language level is 16 (records and `var` are already used). Logging here uses SLF4J; the app does not.
