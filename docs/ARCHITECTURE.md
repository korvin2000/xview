# Architecture

> Paths are relative to `xanderView/src/main/java/org/delafer/xanderView/` unless prefixed with a module name. Per-class notes: [SOURCE_MAP.md](SOURCE_MAP.md).

## 1. Module graph
```
xanderView (app) ──► thirddep-j7 (vendored libs) ──► commons-j7 (utils)        all modules: release 21
      │                    │ provided: jhlabs, commons-j7
      └──► commons-j7      └ compile: vavi-commons (jitpack), jna 5.15
      └──► hppc, jhlabs filters, lz4 1.3 (xxHash), unidecode, opencv-java 4.6.0-5 (WPILib), slf4j-simple
SWT flavor (parent profiles swt-*): all flavors = stock 3.135 classes unpacked at build time + the patched Library (thirddep src/swt-stock); natives from the platform bundle (P7)
            gtk/cocoa = stock 3.135 classes unpacked into thirddep + patched Library (src/swt-stock)
```
Only the app runs maven-shade. Its shaded jar is runnable: `Main-Class: org.delafer.xanderView.gui.MainEntryPoint`, `Enable-Native-Access: ALL-UNNAMED`, and the flavor's `SWT-OS/WS/Arch`.

## 2. Startup sequence
```
MainEntryPoint.main(args)
 ├─ PlatformLaunch.relaunchIfNeeded  → macOS without -XstartOnFirstThread / Linux Wayland without GDK_BACKEND:
 │                                     start the app again with it, exit with its code (PLAN P5.4)
 ├─ System props: sun.java2d.uiScale=1, dpiaware=false
 ├─ Shutdown.addHook()                 → on exit: config.save, OrientationStore.save, CopyLogger.flush,
 │                                        wait for ApplInstance.openTasks==0 (≥1.5 s), CopyService.closeAll, interrupt threads
 ├─ ImageRepository.loadImages()       → SWT icons from /resources
 ├─ OrientationStore.instance().load() → %APPDATA%\xanderView\imagedata.bin
 ├─ CopyService.instance()             → index the current copy target (async)
 └─ new MainWindow().open(path)
      ├─ new CommonContainerExt(path)  → pick a reader, read and sort entries, position the cursor
      ├─ Display + KeyDown filter → bindKeyEvent
      ├─ MultiShell (window shell + fullscreen shell), menu, EMBEDDED Composite → SWT_AWT Frame → ImageCanvas
      └─ runGlobalEventLoop()          → exits when fewer than 2 shells remain; then System.exit(0)
```

## 3. Threading model
| Thread | Work | Notes |
|---|---|---|
| SWT UI (main) | Key events, **navigation decode + scale** (`bindKeyEvent` → `loadImage` is synchronous), splashes, menus | Heavy work runs on the UI thread, so a large image blocks input |
| AWT EDT | `ImageCanvas.repaint`, initial load (`EventQueue.invokeLater`) | Cross into it with `showImage()` |
| `LazyUpdater` | Debounces window resize (100 ms), then `preRenderImage` + repaint | A new thread is created when the old one has died |
| `LazyUpdaterAbstract` | Debounces gamma re-render (750 ms) | |
| `copyService` worker (daemon) | Copy queue, one per target | Counts tasks in `ApplInstance.openTasks` |
| Reader watch threads | `WatchService` on the source and target dirs (registered before the scan) | Playlist callbacks: IO outside, list/cursor changes under the `CommonContainer` monitor (PLAN P3.5) |
| Config timer | Saves config when dirty; flushes `CopyLogger` every 8th tick | |
| Shutdown hook | Persists state | |
To touch SWT widgets from other threads, use `Display.asyncExec`. To touch AWT, use `EventQueue.invokeLater`. Never `syncExec`/`invokeAndWait` from a thread that may hold a container or `CopyService` lock.
`CommonContainer` locking: one monitor guards `images`, `iterator`, `current`, `direction`; navigation steps are atomic; nobody holds it across a splash (nested event loop) or a `CopyService` check (CRC IO). Lock order: container → `CopyService.images`; the watcher holds only the container lock.

## 4. Data model
```
IFileEntry<E> ◄─ IImageEntry<E> (types map ext→ImageType) ◄─ ImageAbstract<E>  {name,size,crc,type,parent,identifier}
   ImageFS      (E=String path)        — rawData via HelperFS (mmap / read prefix)
   ImageZip     (E=Integer index)      — rawData via 7-Zip extract callback
   ImageDec<E>  decorator: .cry → plain    (Encryptor.decrypt; name from header)
   ImageEnc<E>  decorator: plain → .cry    (used only as a copy source)
   ImageAimDec<E> decorator: .aim → first embedded file
Buf        — Closeable ByteBuffer holder. Off-heap memory (an mmap or an archive extract) belongs to a shared FFM `Arena` (`HelperFS.arenaBuf`) that close() frees: deterministic unmap without the `Unsafe` cleaner; an access after close throws instead of crashing. Always close.
IAbstractReader — initialize/read/register(watcher)/getEntryByIdentifier/getComparator/close
   FileReader     — directory scan (commons AbstractFileProcessor), WatchService, optional recursion into archives (sub-SevenZipReaders)
   SevenZipReader — one archive via 7-Zip-JBinding
CommonContainer — SortedLinkedList<ImageAbstract> + ListIterator cursor (direction-aware), loop/beep, last-entry restore
   CommonContainerExt — random mode, only-new mode, start-reached splash
```
**Identity**: `crc = xxHash64(first 3072 bytes, seed=size)`. This is a deliberate approximation: same size plus the same 3072-byte prefix counts as the same image. It keys `OrientationStore` and `CopyService` dedup.
**`equals`** (`ImageAbstract.isEquals`):
- Sizes must match.
- If both CRCs are cached, they decide.
- Otherwise the names are compared.

Subclasses specialize it: `ImageFS` computes the CRCs and falls back to identifiers.

## 5. Image pipeline
```
key → CommonContainerExt.next()/prev() → ImageAbstract
 → ImageLoader.loadImage: switch ImageType
     JPEG  → codec/TurboJpeg (FFM: tj3DecompressHeader → denom 1/2/4/8 from getDenom(image vs screen) → tj3Decompress8 into the image's byte[]) ─fail/LinkageError→ ImageIO
     AIM   → recurse with ImageAimDec
     WEBP  → WebPCodec.decodeImage (vendored webp4j decoder, JNI, packed ARGB/RGB int image)
     other → ImageIO.read (AVIF/JXL via META-INF/services in thirddep-j7)
 → caption (+"*" if CopyService.exists) → ImageCanvas.setImage(img, text, OrientationStore.getImageData(crc))
 → preRenderImage: target = fit(img, canvas) × 1.05^zoom
                   → ScaleFactory.instance(imgSize).resize (ScalerHelper picks IResizer by pixels)
                   → Rotator2D.rotate(orientation) → GammaCorrection (LUT)
 → showImage() → EDT repaint → paintCanvas (centered + pan×16 px, caption)
```

## 6. Native loading (the reason the libraries are vendored)
**Design intent (from the owner):** stock SWT, 7-Zip-JBinding, OpenCV and similar jars ship natives for many OSes and extract them themselves on every start. xanderView instead loads every native through one routine, `net.j7.commons.jni.LibraryLoader`, from a **persistent per-user cache**, and ships **one small package per platform** (no fat jar). Since P7 the natives are unmodified upstream builds, pinned by SHA-256 in `natives/<platform>.tsv` and shipped as bundles next to the jar ([NATIVES.md](NATIVES.md)); hand-built files remain possible as an override. Every native-using library is vendored and its loader is patched to call `LibraryLoader`. The same reason explains the **small classes-only OpenCV jar** (WPILib 4.6.0-5, 326 KB) instead of a 100 MB+ bundled-natives distribution.

`LibraryLoader.loadLibrary(name)` / `checkLibrary(name, load)` / `tryLoadLibrary(name)` (returns whether it loaded; used by the stock-SWT `Library`):
0. **File name** (`fileName`, PLAN P5.2): the load name's candidates are `<name>.<ext>` and, on Linux/macOS, `lib<name>.<ext>`; macOS tries `.dylib` then `.jnilib` (JNA libraries only `.dylib`). In manifest mode the first candidate listed in `natives.sha256` wins; otherwise the first candidate present in the **source**, else the first present in the cache, else the first one (reported missing). Windows has exactly one candidate, `<name>.dll`. Memoized per JVM.
1. **Source:** `<location>/libs/<os>/<arch>/`. `location` comes from, in order: `-Dxview.libs.location`, the environment variable `XVIEW_LIBS_LOCATION`, `location` in `/libs.properties` (empty since P7), **the directory of the jar that contains `libs.properties` if it has `libs/<os>/<arch>/`** (the dist layout), else that jar itself (`jar:` URL). Running from class directories (IDE): the directory that contains the classes root holding the classpath manifest `libs/<os>/<arch>/natives.sha256` if it has `libs/<os>/<arch>/` (the Maven build stages the bundle in `xanderView/target/libs/`), else "cache only".
2. **Manifest mode** (the source has `natives.sha256`, `sha256sum` format; optional `natives.deps`): bundle id = first 12 hex of SHA-256 over the manifest text; cache dir `<AppData>/libs/<os>/<arch>/<bundle12>/`. On a library's first request the file is copied to a sibling temp file, its SHA-256 is verified against the manifest, and it is moved into place atomically (`FileAlreadyExists`/`AccessDenied` = another instance published the same verified content). A file already in the bundle dir is trusted (one `stat` per library per start). If `System.load` fails on a trusted file, the hash is re-checked: mismatch → the file is deleted and copied again once; intact → the `UnsatisfiedLinkError` propagates (state FAILED). Bundle dirs are immutable, so several installed versions share one cache root; nothing is swept automatically.
   - **Authority:** the jar carries the manifest it was built with (`/libs/<os>/<arch>/natives.sha256` + `natives.deps`, from the build's bundle). For the default location the jar's copy is authoritative: a source whose manifest differs is logged as an error and only files matching the jar's hashes are trusted (a tampered file is "checksum mismatch", not loaded). An explicit `-Dxview.libs.location`/`XVIEW_LIBS_LOCATION` is the owner's override: the source's own manifest is authoritative and a difference is logged once as a warning. File names in manifests must be plain names.
   - **macOS:** the `com.apple.quarantine` attribute is removed from the copy (`Files.copy` may clonefile and keep it).
3. **Legacy mode** (no `natives.sha256` in the source): the flat directory `<AppData>/libs/<os>/<arch>/<file>`, copied when missing or when the content differs from the source (exact comparison: `Files.mismatch` for local files, a streamed compare otherwise; 21–28 ms for the 8 DLLs warm). Publishing: sibling temp file, verified, `ATOMIC_MOVE`; a DLL mapped by a running instance is renamed to `<file>.old-<nanos>` first; if publishing fails the loader loads the local source file directly, or, as a last resort, the stale cache file with a warning. Leftovers older than 10 minutes are swept once per JVM. The owner's deployed `C:\Program Files\xview\libs` (no manifest) keeps working this way.
4. **Dependencies:** `natives.deps` lines (Windows arm64: `libavif.dll: aom.dll dav1d.dll`) plus the built-in Windows entry `opencv_java460.dll → concrt140.dll?` (`?` = optional, loaded only when the bundle has it; the JDK does not ship `concrt140`). Dependencies are made available the same way and `System.load`-ed first by absolute path (recursively), so the dynamic linker resolves the dependent's import by module name / soname / install name against the loaded image. File names must therefore equal the soname (`libhwy.so.1`), not the full version.
5. **Load:** `load=true` → `System.load(file)` (JNI; Unix: the published file is made `rwxr-xr-x`). `load=false` → only makes the file available and sets `jna.library.path` / `jna.platform.library.path` to its folder (JNA: `libavif`). Per-JVM state per load name: MISSING / COPIED / LOADED / FAILED; a later `loadLibrary` promotes COPIED to LOADED. The method is `synchronized` (re-entrant).
6. **Soft failure:** a missing source with no cache logs `doesn't exist: …` once (warning) and returns `false` without throwing; the failure surfaces later as `UnsatisfiedLinkError`. A present but incompatible native throws from `System.load` immediately. Callers that must degrade: `ImageLoader` (JPEG → ImageIO), `ScaleFactory` (OpenCV → Lanczos3), the stock-SWT `Library` (`tryLoadLibrary` → `UnsatisfiedLinkError`).
7. **Diagnostics (B10):** `report()` gives one line per file of the jar's manifest (`not requested | COPIED | LOADED | MISSING | FAILED`) plus the source, mode and bundle id; `checkAll()` loads every manifest file in order (dependencies first; `jawt` is preloaded for `swt-awt`) and returns the failures. `java -jar xanderview.jar --check-natives` (`gui/NativeCheck`) prints both and exits 1 on a failure; it runs before `PlatformLaunch`.

| | Windows | Linux | macOS |
|---|---|---|---|
| `<AppDataDir>` (first writable, `io/SysInfo`) | `%APPDATA%\xanderView` (e.g. `C:\Users\<u>\AppData\Roaming\xanderView`) | `~/.local/share/.xanderView` (note the dot; P7.8) | `~/Library/Application Support/xanderView` |
| `<os>` / `<ext>` | `windows` / `dll` | `linux` / `so` | `macos` / `dylib`, then `jnilib` (JNA: `dylib` only) |
| `<arch>` (from `os.arch`; `arm64` → `aarch64`) | `amd64` (x86_64 is mapped to amd64), `aarch64` | `x86_64` (amd64 is mapped to x86_64), `aarch64` | `x86_64`, `aarch64` |
| Names | The load name is the literal file stem, so `lib` prefixes are part of the name (`lib7-Zip-JBinding`, `libavif`, `libjxl_jni`) | `<name>` or `lib<name>` (`turbojpeg` → `libturbojpeg.so`) | as Linux |

Load sites (Windows names):
- JNI:
  - SWT `internal/Library.loadLibrary` (stock 3.135, patched, all flavors): `tryLoadLibrary` for `<name>-<platform>-<version>` (e.g. `swt-win32-4974r10`, `swt-pi3-gtk-4974r10`), `<name>-<platform>`, `<name>`; throws `UnsatisfiedLinkError` if none loads. Windows loads `swt-osversion-win32-4974r10` and `swt-win32-4974r10` at startup, `swt-awt-win32-4974r10` at `SWT_AWT.new_Frame` (plus the JDK's `jawt`), `swt-gdip-win32-4974r10` lazily
  - `SevenZip.ensureLibraryIsInitialized` (26.03-2.3): `lib7-Zip-JBinding`, then `initLoadedLibraries()`
  - `NativeWebP` (static, vendored webp4j): `webp4j`
- FFM (`java.lang.foreign`, no JNI exports needed):
  - `codec/TurboJpeg.Lib` (static): `LibraryLoader.loadLibrary("turbojpeg")`, then `SymbolLookup.loaderLookup()` binds `tj3Init/Destroy/Set/Get/GetErrorStr/SetScalingFactor/DecompressHeader/Decompress8`; the two calls that take heap arrays are `Linker.Option.critical(true)` (zero-copy; a safepoint waits for the decode, ≈0.1 s per 24 MP). Any libturbojpeg ≥ 3.0 build works (official or the owner's optimized ones).
  - `JpegXLImageReader` (static): `libjxl_jni`
  - `ResizerNewOpenCV` (static): `opencv_java460`
- JNA: `Avif` calls `checkLibrary("libavif", false)`, then `Native.load("avif")`. JNA on Windows also tries the `lib` prefix, which finds `libavif.dll`.
- Outside the loader: JNA extracts its own `jnidispatch` (P7.8); `Hasher` uses the pure-Java xxHash (`XXHashFactory.safeInstance()`), so lz4-java extracts nothing.

Full platform matrix, bundle format and owner workflows: [NATIVES.md](NATIVES.md); the Windows file table: [DEPENDENCIES.md#natives](DEPENDENCIES.md#natives).

## 7. Extension points (how to add …)
| Add | Touch |
|---|---|
| An image format | `ImageType` enum + `IImageEntry.types` map; a decoder branch in `gui/ImageLoader`, or just an ImageIO SPI in `thirddep-j7/src/main/resources/META-INF/services`; the file dialog filter in `MainWindow.createFileMenu`; SPEC §2 |
| An archive type | `file/readers/ArcTypes` (7-Zip must support it); SPEC §2 |
| A resizer | Implement `scale/IResizer`; give it an id in the `ScaleFactory.SCALER` enum and a switch branch; SPEC §8 |
| A key binding | `MainWindow.bindKeyEvent` (prefer `SWT.*` constants to raw ints); SPEC §5 |
| A config key | A constant + default in `ApplConfiguration.defaults`; SPEC §8 |
| A sort type | `ApplConfiguration.SortType` + `comparator/ComparatorFactory`; SPEC §3 |
| Persisted per-image state | `OrientationStore` `Range` (bits 28–63 are free); a format change needs a migration; SPEC §7 |

## 8. Patterns and idioms
- Singletons use the Bill-Pugh `Holder` idiom: `ApplConfiguration`, `OrientationStore`, `ScaleFactory`, `Hasher`, `LibraryLoader`. `CopyService` is a multiton keyed by target path.
- The decorator pattern is used for entry transforms (`ImageDec`/`ImageEnc`/`ImageAimDec`).
- Strategy: `IResizer`, `IAbstractReader`, `INameIncrementer`, `Comparator`s via `ComparatorFactory`.
- Observer: `ContentChangeWatcher` (reader → container), `CopyService.Observer`/`CopyObserver` (copy → splash and caption).
- hppc primitive maps (`LongLongHashMap`, `IntFloatHashMap`) keep memory low. `ApplConfiguration` has comments with benchmarks.
