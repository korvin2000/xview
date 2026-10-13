---
paths:
  - "commons-j7/**"
---
# commons-j7: shared utilities

- Consumers are `thirddep-j7` (patched native loading) and `xanderView`. Before changing any public signature, grep both trees (`net.j7.commons.<Class>`). The used set is in `docs/DEPENDENCIES.md` §3.
- **`jni/LibraryLoader` is the heart of the custom native design.** All natives (Windows: `swt-*`, `lib7-Zip-JBinding`, `turbojpeg` (FFM), `webp4j`, `libavif`, `libjxl_jni`, `opencv_java460`) go through it, so a regression breaks the app at startup. These are deployment contracts (details: `docs/NATIVES.md` §4, `docs/ARCHITECTURE.md` §6):
  - the source `<location>/libs/<os>/<arch>/<file>`, with `location` from `-Dxview.libs.location` > env `XVIEW_LIBS_LOCATION` > `/libs.properties` (empty) > the jar's directory if it has `libs/<os>/<arch>/` > the jar itself (`jar:`); from class directories: the parent of the classes root holding the classpath manifest if it has `libs/<os>/<arch>/` (`target/` of a Maven build), else cache only
  - the file per load name: Windows exactly `<name>.dll`; Unix also `lib<name>`; macOS `.dylib` then `.jnilib` (JNA only `.dylib`); in manifest mode the first candidate listed in `natives.sha256` wins, otherwise a source candidate beats a cache-only candidate
  - the cache root `<AppDataDir>/libs/<os>/<arch>/`, resolved by `io/SysInfo` (Windows `%APPDATA%\xanderView`); **manifest mode** adds `<bundle12>/` (first 12 hex of sha256 over `natives.sha256`): copy once, verify, trust, never modify, no automatic sweep; **legacy mode** (no manifest) is the flat directory with the exact content compare, atomic temp publish, rename-old fallback for mapped DLLs and load-from-source fallback
  - the os dirs (`windows`, `linux`, `macos`) and arch names (`amd64`, `x86_64`, `aarch64`; `arm64` maps to `aarch64`)
  - the manifest files `natives.sha256` (sha256sum format) and `natives.deps` (`<file>: <dep> <dep>…`, `?` = optional); the jar's copy is the authority for the default location, an explicit location's own manifest is the owner's override; dependencies are `System.load`-ed first by absolute path; built-in optional dependency `opencv_java460.dll → concrt140.dll` on Windows
  - the soft-fail semantics (`tryLoadLibrary` reports the result for callers that must fail fast, e.g. the stock-SWT `Library`); `report()` and `checkAll()` back `--check-natives`

  P5.2, P7.2 and B6/B7/B8/B9/B10 are done.
- The dead packages were deleted (PLAN P2.3); some unused classes remain in the kept packages. Don't extend them. Keep `jni`, `io.SysInfo`, `io.FilePath`, `io.helpers.WildcardMatcher`.
- Language level is 26 (`release` from the parent pom). Logging uses SLF4J (`slf4j-api` only; the `slf4j-simple` binding is in the app).
