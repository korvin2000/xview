# SPEC — observable behavior (as implemented)

> This file describes the current code, not wishes. Keep it in sync with it (see the doc-sync rule in [CLAUDE.md](CLAUDE.md)).
> Code anchors use `pkg/Class` relative to `xanderView/src/main/java/org/delafer/xanderView/`.

## 1. Launch — `gui/MainEntryPoint`, `file/CommonContainer`
- Command line: `java -jar xanderview-j7-1.0.0.jar [path]`. `path` can be an image file, a directory, or an archive. Surrounding quotes are stripped.
- With no path, the app reads `_zlst.csv` in the **current working directory**. The file holds `path[,entryIndex]`; the drive letter is stripped from the path. If the file doesn't exist, the CWD is used.
- When a file is given, its folder (or archive) is opened and positioned on that file. Unverified: `FileReader.getContainerPath` returns an empty path for a regular-file argument ([ADDITIONAL_INFO](docs/ADDITIONAL_INFO.md) F7).
- At startup the app forces Java2D UI scale to 1 (DPI-unaware) and registers a shutdown hook that saves all state (§7).

## 2. Sources and formats — `interfaces/IImageEntry.types`, `file/readers/*`
| Extension | Type | Decoder / handling |
|---|---|---|
| jpg jpeg jpe jfif jif jfi | JPEG | TurboJPEG, falling back to ImageIO on an `Exception`. A missing or incompatible `turbojpeg` DLL throws `UnsatisfiedLinkError` (a `LinkageError`), which is **not** caught. Downscaled 1/2, 1/4 or 1/8 in the DCT when the image ≥ 2× the screen. Fast DCT and upsampling for files ≥ 1.75 MB. |
| bmp rle dib, png | BMP, PNG | ImageIO |
| webp, avif, jxl | WEBP, AVIF, JXL | ImageIO through thirddep SPIs (needs DLLs) |
| aim | AIM | A container. The first embedded image is extracted (`file/entry/ImageAimDec`). |
| cry | ENCRYPTED | Decrypted on read (§6). Listed only if the key file exists. |
| xav | XAV | A "virtual directory" (`HelperFS.isVirtualDirectory`) |
| zip rar 7z jzip | archive | 7-Zip-JBinding (`SevenZipReader`). It is opened directly, or entered from a directory when `recursive=true`. |
- Directory sources are watched (WatchService). New image files are inserted into the sorted list and deleted ones are removed, live.
- `filterCount=N` (N > 0): each folder or archive contributes only N evenly spaced images (`common/Selector`).

## 3. Navigation — `file/CommonContainer`, `file/CommonContainerExt`
- Order comes from `sort=<type>[,asc|desc]`. Types: `name`, `natural` (alphanumeric), `size`, `date`/`datetime`/`time`, `random`, `unsorted`/`no`.
- With `loop.source=true`, the list wraps at either end with a beep (`/audio/sound01.wav`).
- A splash appears when the start position is reached again.
- **Random mode** (`P`): the list is reshuffled with SecureRandom. Toggling again restores the sort.
- **Only-new mode** (`Q`): skips images that already exist in the current copy target. Jumps of ±10 become ±1.

## 4. View — `gui/ImageCanvas`
- The image is fitted to the canvas while keeping its aspect ratio. It is **auto-rotated left** when its orientation (landscape or portrait) differs from the canvas and its aspect ratio is ≥ 1.05.
- Zoom: 31 steps each way, factor 1.05 per step. Pan: 31 steps each way, 16 px per step. Gamma: 31 steps each way, factor 1.05 per step (a LUT).
- Rotate and flip compose through the state machine `orientation/CommonRotator.Orientation`.
- Caption at the bottom left: `shortName [index/total]`. Names longer than 50 chars are shortened to 24 chars + `~` + 24 chars. The caption is **green**, or **red with `*`** when the image already exists in the current target. It is drawn rotated 90° in fullscreen.
- Fullscreen toggles with `Enter` or a double-click. Its state is persisted in `fullscreen`.
- Resizer choice depends on the image's pixel count (§8, `Scaler`).

## 5. Keys — `gui/MainWindow.bindKeyEvent` (`SWT.*` constants and character literals; keep this table in sync)
| Key | Action |
|---|---|
| → / PgDn | next image |
| ← / PgUp / Backspace | previous image |
| ↑ / ↓ | +10 / −10 images |
| Esc | quit (after 250 ms) |
| Enter, double-click | toggle fullscreen |
| Numpad + / − / * | zoom in / zoom out / reset zoom, pan and gamma |
| Numpad 8 / 2 / 4 / 6 | pan up / down / left / right |
| `2` / `1` | gamma up / down (debounced re-render, 750 ms) |
| `K` `L` `;` / `R` | rotate left / rotate right |
| `V` / `H` | flip vertical / flip horizontal |
| `S` | save the current orientation, zoom, pan and gamma for this image (keyed by CRC) |
| `P` | toggle random mode |
| `Q` | toggle only-new mode |
| Space | show the current copy target |
| `M` / `N` | next / previous copy target (re-checks the `*` state) |
| F1 | copy **encrypted** (`.cry`); falls back to the original if there is no key |
| F7 / F8 / F9 | copy **decrypted** (plain image) |
| F3 | copy **as-is** (encrypted stays encrypted, plain stays plain) |
| Ctrl+O / Ctrl+D | open an image (dialog) / choose the copy target directory (dialog) |
Other keys are logged at debug level (`unbound key code: <n>`; enable with `-Dorg.slf4j.simpleLogger.defaultLogLevel=debug`).

## 6. Copy and encryption — `file/CopyService`, `file/entry/Encryptor`, `CipherKey`
- `target.folder` holds one or more directories separated by `;`. `M`/`N` cycle through them.
- Each target gets a `CopyService` instance that indexes the target (CRC by content, kept live by a watcher). If the target already holds the same content (same size + CRC), the copy returns `Ignore`. A name conflict gets an incrementing suffix (`common/DefaultNameIncrementer`).
- A copy is queued on a worker thread, and the result shows as a splash: `State` Success/Ignore/Error/Special1.
- When `history=true`, each copy is logged to `<target>/history_dd_MM_yy.log` (`common/CopyLogger`, buffered).
- **`.cry` layout** (big-endian). Let `F` = image bytes, `N` = UTF-8 basename bytes, `P = (16 − ((F+2+N) mod 16)) mod 16`, `L = N+P`:

  `int32 F | byte alg=1 | AES-128/ECB/NoPadding( int16(clamp(L,0,32767) − 32768) | nameBytes | P zero bytes | imageBytes )`

  - The *whole* encrypted input is padded to a 16-byte multiple, and the stored length includes the padding.
  - The length uses a **signed-short bias** (`ByteUtils.UIntToByte2`: L=6 → `80 06`), not plain uint16.
  - Key = first 16 bytes of `SHA-256("z!a3" + trim(contents of file at cry.keyfile))`.
  - `decrypt0` doesn't validate the alg byte or the length, and there is no authentication. Treat it as a legacy privacy feature with no integrity guarantee.

  **Persisted format. Do not change without a migration.** Details: [docs/ADDITIONAL_INFO.md §6](docs/ADDITIONAL_INFO.md).

## 7. Files on disk
| File | Location | Writer | Format |
|---|---|---|---|
| `xander.properties` | `%APPDATA%\xanderView\` (first writable app-data dir, `SysInfo.getAppDataDir`) | `ApplConfiguration` (saved by a timer when dirty, and at exit) | Java properties (§8) |
| `imagedata.bin` | same dir | `OrientationStore` at exit (temp file + replace) | Big-endian pairs of `long crc, long packed`. There is no header, version or checksum. Packed bits:<br>• 0–3: orientation as **ordinal+1** (0 = unset; the `Orientation` enum order is part of the format)<br>• 4–9: zoom<br>• 10–15: pan-x<br>• 16–21: pan-y<br>• 22–27: gamma<br>Each 6-bit field maps 0 → 0, −31..−1 → 1..31 (`v+32`), +1..+31 → 32..62 (`v+31`). |
| `libs\<os>\<arch>\<bundle12>\*` | same dir | `LibraryLoader` manifest mode (dist zips): a content-addressed per-bundle cache, each file copied once and verified against `natives.sha256`, never modified afterwards; one directory per installed bundle, not swept automatically | the natives of the bundle (Windows: 7 DLLs) |
| `libs\<os>\<arch>\*` | same dir | `LibraryLoader` legacy mode (source without `natives.sha256`): a flat cache, re-copied when the source content differs (exact compare, atomic publish) | the natives (Windows: `libs\windows\amd64\*.dll`) |
| `_zlst.csv` | CWD | `ApplConfiguration.save` when `csv=1` | `lastEntryId` |
| `history_dd_MM_yy.log` | copy target | `CopyLogger` | one line per copy |

## 8. Config keys (`xander.properties`) — `gui/config/ApplConfiguration`
| Key | Default | Meaning |
|---|---|---|
| `PosX` `PosY` `Width` `Height` | 0 0 800 600 | Window bounds. 0,0 means center on the monitor. |
| `fullscreen` | `1` | Start in fullscreen |
| `loop.source` | `true` | Wrap at the ends |
| `sort` | `size,desc` | §3 |
| `recursive` | false | Enter subfolders and archives |
| `filterCount` | 0 | §2 sampling |
| `target.folder` | `A:\target\src` | Copy targets, separated by `;` |
| `cry.keyfile` | `A:\key` | Path to the key file. Missing or empty disables `.cry`. |
| `csv` | `1` | Write `_zlst.csv` |
| `history` | false | Write the copy log |
| `Scaler` | `1;11,500000;10,665000;6,1850000;5,2500000;6,6000000;4,8000000;2,13000000` | `type[,maxPixels or WxH]` items separated by `;` and sorted by size. The first item whose size ≥ the image's pixel count wins. Items without a size are fallbacks; the first one wins. Type ids: 1 AWT nearest, 2 AWT bilinear, 3 AWT bicubic, 4 CV linear, 5 CV area, 6 CV bicubic, 7 CV Lanczos4, 8 Nobel Hermite, 9 Mitchell, 10 Bicubic-HF, 11 Lanczos3, 12 Nobel multistep. Before 2026-10-12 the default had the typo `6;6000000` (PLAN B1). Existing user files keep whatever value they hold. |

## 9. Non-functional
- **Platforms:**
  - Implemented: Windows 10 (build 14393+, required by stock SWT 3.135's `swt-osversion` check) x64; Linux/macOS dist zips exist but are unverified.
  - Required targets: Linux and macOS x86_64; optional: aarch64 on all three. Their jars build (`-Dswt.flavor`, SWT 3.135 gtk/cocoa), but the natives don't exist yet (PLAN P5.3).
- **Runtime:** JDK/JRE 26+. The deployed install still bundles Liberica JRE 23, which cannot run jars built since the JDK 26 migration (PLAN P6). `java -jar` needs no flags: the manifest has `Enable-Native-Access: ALL-UNNAMED` (the former `Add-Exports: java.desktop/sun.awt.windows` went with the vendored SWT 4.527 in P7).desktop/sun.awt.windows` (SWT_AWT) and `Enable-Native-Access: ALL-UNNAMED`. A classpath/IDE launch without `--add-exports=java.desktop/sun.awt.windows=ALL-UNNAMED` restarts itself once with it. On macOS the app restarts itself once with `-XstartOnFirstThread`, and on Linux under Wayland with `GDK_BACKEND=x11` (`-Dxview.relaunch=false` disables this).
- **Natives:** 8 custom DLLs on Windows: `swt`, `lib7-Zip-JBinding`, `libgcc_s_seh-1`, `turbojpeg`, `webp-imageio`, `libavif` (JNA), `libjxl_jni`, `opencv_java460`.
  - They are copied on first use from `<location>/libs/<os>/<arch>/` (default: `libs/` next to the jar, else inside the jar; override `-Dxview.libs.location` / env `XVIEW_LIBS_LOCATION`) into the persistent cache `%APPDATA%\xanderView\libs\<os>\<arch>\…` (§7). `java -jar xanderview.jar --check-natives` loads every native of the shipped manifest headless and exits 1 if one fails ([docs/NATIVES.md](docs/NATIVES.md)).
  - Missing libraries fail softly: the feature breaks with `UnsatisfiedLinkError`. See [docs/DEPENDENCIES.md](docs/DEPENDENCIES.md#natives).
- **Logging:** SLF4J with `slf4j-simple` to stderr at level `info` (config `simplelogger.properties`; override with `-Dorg.slf4j.simpleLogger.*`). Vendored libraries may still print directly.
