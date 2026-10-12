# Architecture

> Paths are relative to `xanderView/src/main/java/org/delafer/xanderView/` unless prefixed with a module name. Per-class notes: [SOURCE_MAP.md](SOURCE_MAP.md).

## 1. Module graph
```
xanderView (app) ──► thirddep-j7 (vendored libs) ──► commons-j7 (utils)        all modules: release 21
      │                    │ provided: jhlabs, commons-j7
      └──► commons-j7      └ compile: vavi-commons (jitpack), jna 5.15
      └──► hppc, jhlabs filters, lz4 1.3 (xxHash), unidecode, opencv-java 4.6.0-5 (WPILib), slf4j-simple
SWT flavor (parent profiles swt-*): win32 = vendored 4.527 sources (thirddep src/swt-win32);
            gtk/cocoa = stock 3.135 classes unpacked into thirddep + patched Library (src/swt-stock)
```
Only the app runs maven-shade. Its shaded jar is runnable: `Main-Class: org.delafer.xanderView.gui.MainEntryPoint`, `Add-Exports: java.desktop/sun.awt.windows`, `Enable-Native-Access: ALL-UNNAMED`, and the flavor's `SWT-OS/WS/Arch`.

## 2. Startup sequence
```
MainEntryPoint.main(args)
 ├─ PlatformLaunch.relaunchIfNeeded  → Windows classpath launch without the sun.awt.windows export / macOS without -XstartOnFirstThread / Linux Wayland without GDK_BACKEND:
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
Buf        — Closeable ByteBuffer holder (may wrap a mapped buffer + channel). Always close.
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
     JPEG  → TJDecompressor (scaling denom 1/2/4/8 from getDenom(image vs screen)) ─fail→ ImageIO
     AIM   → recurse with ImageAimDec
     other → ImageIO.read (WebP/AVIF/JXL via META-INF/services in thirddep-j7)
 → caption (+"*" if CopyService.exists) → ImageCanvas.setImage(img, text, OrientationStore.getImageData(crc))
 → preRenderImage: target = fit(img, canvas) × 1.05^zoom
                   → ScaleFactory.instance(imgSize).resize (ScalerHelper picks IResizer by pixels)
                   → Rotator2D.rotate(orientation) → GammaCorrection (LUT)
 → showImage() → EDT repaint → paintCanvas (centered + pan×16 px, caption)
```

## 6. Native loading (the reason the libraries are vendored)
**Design intent (from the owner):** stock SWT, 7-Zip-JBinding, OpenCV and similar jars ship natives for many OSes and extract them themselves. xanderView instead uses **custom, performance-optimized native builds**, some compiled and copied by hand, that are loaded by one routine and **stored persistently** per user. To make that work, every native-using library is vendored and its loader is patched to call `net.j7.commons.jni.LibraryLoader`. The same reason explains the **small classes-only OpenCV jar** (`xanderView/opencv/opencv-460.jar`, a WPILib 4.6.0-5 build, 326 KB) instead of a 100 MB+ bundled-natives distribution.

`LibraryLoader.loadLibrary(name)` / `checkLibrary(name, load)` / `tryLoadLibrary(name)` (returns whether it loaded; used by the stock-SWT `Library`):
0. **File name** (`fileName`, PLAN P5.2): the load name's candidates are `<name>.<ext>` and, on Linux/macOS, `lib<name>.<ext>`; macOS tries `.dylib` then `.jnilib` (JNA libraries only `.dylib`). The first candidate present in the **source** wins, else the first present in the cache, else the first one (reported missing). Windows has exactly one candidate, `<name>.dll`. Memoized per JVM.
1. **Target (persistent cache):** `<AppDataDir>/libs/<os>/<arch>/<file>`.
2. **Copy on change (lazy, per library, on its first request in a JVM):**
   - **Source:** `<location>/libs/<os>/<arch>/<file>`. `location` (a directory path, `file:`, `jar:` or `http` URL) comes from, in order: the system property `xview.libs.location`, the environment variable `XVIEW_LIBS_LOCATION`, `location` in `/libs.properties` on the classpath, else the jar that contains `libs.properties` (PLAN P1.9).
   - **When it copies:** when the cache file is missing, or its **content differs** from the source. The check is an exact comparison: `Files.mismatch` for local files, a streamed compare otherwise. It measured about 21–28 ms for all 8 DLLs, with a warm OS file cache.
   - **How it publishes:** writes a sibling temp file `<file>.<pid>-<nanos>.tmp`, then does `ATOMIC_MOVE`. For local-file sources the temp file is verified byte by byte; stream sources rely on a complete read with no exception.
   - **If the target is in use:** when the old DLL is mapped by a running instance, it is renamed to `<file>.old-<nanos>` and the new one moved in.
   - **If publishing fails:** the loader first accepts a concurrent winner with equal content, then loads **directly from the local source file** (this avoids a stale cache copy; it is not a cross-library version guarantee). As a last resort it loads the stale cache with a warning. If the source exists but cannot be read, the existing cache is used; an unreadable *cache* file is treated as stale (replaced, or the source is loaded).
   - **Cleanup:** leftovers older than 10 minutes are swept once per JVM.
   - **Co-location** (`resolve`): if a Windows dependency had to come from the source directory, the library is loaded from there too, so its imports resolve next to it. A load that fails because the file vanished mid-swap is resolved and retried once.
3. **Load:**
   - `load=true` → `System.load(file)` (JNI). Unix: the published file is made `rwxr-xr-x` without spawning `chmod`.
   - `load=false` → only makes the file available and sets `jna.library.path` / `jna.platform.library.path` to its folder (JNA libs).
   - Per-JVM state per name is MISSING / COPIED / LOADED. A later `loadLibrary` promotes COPIED to LOADED, and a failed `System.load` is retried on the next call. The method is `synchronized` (re-entrant).
4. **Windows dependencies:** requesting `libavif` also makes `libgcc_s_seh-1` available next to it (copy only; `WINDOWS_DEPENDENCIES`). This is preventive: the current libavif 1.1.1 loads without it (PLAN B9).
5. **Soft failure:**
   - A missing source with no cache logs `doesn't exist: …` once (warning) and returns `false` without throwing. The failure surfaces later as `UnsatisfiedLinkError`.
   - A present but incompatible DLL throws from `System.load` immediately.

| | Windows | Linux | macOS |
|---|---|---|---|
| `<AppDataDir>` (first writable, `io/SysInfo`) | `%APPDATA%\xanderView` (e.g. `C:\Users\<u>\AppData\Roaming\xanderView`) | `~/.local/share/.xanderView` (note the dot) | `~/Library/Application Support/xanderView` |
| `<os>` / `<ext>` | `windows` / `dll` | `linux` / `so` | `macos` / `dylib`, then `jnilib` (JNA: `dylib` only) |
| `<arch>` (from `os.arch`; `arm64` → `aarch64`) | `amd64` (x86_64 is mapped to amd64), `aarch64` | `x86_64` (amd64 is mapped to x86_64), `aarch64` | `x86_64`, `aarch64` |
| Names | The load name is the literal file stem, so `lib` prefixes are part of the name (`lib7-Zip-JBinding`, `libavif`, `libjxl_jni`) | `<name>` or `lib<name>` (`turbojpeg` → `libturbojpeg.so`) | as Linux |

Load sites (Windows names):
- JNI:
  - SWT `internal/Library.loadLibrary` (win32 4.527): `swt`, plus soft-failing `swt-gdip` and `swt-awt`. Stock-SWT flavors (gtk/cocoa 3.135, patched `Library`): `tryLoadLibrary` for `<name>-<platform>-<version>` (e.g. `swt-pi3-gtk-4974r10`), `<name>-<platform>`, `<name>`; throws `UnsatisfiedLinkError` if none loads
  - `SevenZip.ensureLibraryIsInitialized`: `lib7-Zip-JBinding`, `libgcc_s_seh-1`, then `initLoadedLibraries()`
  - `TJLoader`: `turbojpeg` (direct `LibraryLoader` call since P5.1)
  - `WebP.loadNativeLibrary`: `webp-imageio`
  - `JpegXLImageReader` (static): `libjxl_jni`
  - `ResizerNewOpenCV` (static): `opencv_java460`
- JNA: `Avif` calls `checkLibrary("libavif", false)`, then `Native.load("avif")`. JNA on Windows also tries the `lib` prefix, which finds `libavif.dll`.

Full DLL table and platform status: [DEPENDENCIES.md#natives](DEPENDENCIES.md#natives).

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
