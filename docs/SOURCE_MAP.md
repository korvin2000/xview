# Source map

> One line per class. **St** (status): `A` = on the runtime path, `D` = dead (unreferenced by main code), `T` = used only by tests or scratch code, `X` = deprecated or legacy package. LOC is approximate.
> Rows link to the file. Keep this file in sync: `bash tools/check-docs.sh` reports missing classes and broken links.

## xanderView — `xanderView/src/main/java/org/delafer/xanderView/`

### gui — entry, window, canvas
| Class | LOC | St | Role |
|---|---|---|---|
| [MainEntryPoint](../xanderView/src/main/java/org/delafer/xanderView/gui/MainEntryPoint.java) | 65 | A | `main`: `PlatformLaunch`, DPI props, shutdown hook, preload stores, opens `MainWindow` |
| [MainWindow](../xanderView/src/main/java/org/delafer/xanderView/gui/MainWindow.java) | 480 | A | SWT Display/shell setup, menu, SWT_AWT embedding, **all key bindings** (`bindKeyEvent`, `SWT.*` constants and chars), event loop |
| [ImageLoader](../xanderView/src/main/java/org/delafer/xanderView/gui/ImageLoader.java) | 153 | A | Base of MainWindow. Decodes an entry (TurboJPEG/ImageIO), builds the caption, hands the image to the canvas |
| [ImageCanvas](../xanderView/src/main/java/org/delafer/xanderView/gui/ImageCanvas.java) | 313 | A | AWT Canvas. Fit/zoom/pan/gamma/orientation state, `preRenderImage` pipeline, caption painting |
| [PlatformLaunch](../xanderView/src/main/java/org/delafer/xanderView/gui/PlatformLaunch.java) | 120 | A | First step of `main`: relaunches once with `-XstartOnFirstThread` (macOS) or `GDK_BACKEND=x11` (Linux under Wayland) when missing |

### gui/config — settings and persisted state
| Class | LOC | St | Role |
|---|---|---|---|
| [ApplConfiguration](../xanderView/src/main/java/org/delafer/xanderView/gui/config/ApplConfiguration.java) | 354 | A | `xander.properties`: keys, defaults, autosave timer, copy-target rotation, key-file password, `SortBy` |
| [ApplInstance](../xanderView/src/main/java/org/delafer/xanderView/gui/config/ApplInstance.java) | 14 | A | Globals: `openTasks` counter, `_zlst.csv` name, `lastEntry` |
| [OrientationStore](../xanderView/src/main/java/org/delafer/xanderView/gui/config/OrientationStore.java) | 253 | A | `imagedata.bin`: CRC → packed orientation/zoom/pan/gamma (bit `Range`s). **Persisted format** |

### gui/helpers, gui/splash
| Class | LOC | St | Role |
|---|---|---|---|
| [MultiShell](../xanderView/src/main/java/org/delafer/xanderView/gui/helpers/MultiShell.java) | 191 | A | Pair of a windowed shell and a fullscreen shell. `active()`, fullscreen switching, listener re-binding |
| [LazyUpdater](../xanderView/src/main/java/org/delafer/xanderView/gui/helpers/LazyUpdater.java) | 58 | A | Resize debounce thread (100 ms) → re-render |
| [LazyUpdaterAbstract](../xanderView/src/main/java/org/delafer/xanderView/gui/helpers/LazyUpdaterAbstract.java) | 57 | A | Generic debounced task (750 ms). Used for gamma |
| [ImageRepository](../xanderView/src/main/java/org/delafer/xanderView/gui/helpers/ImageRepository.java) | 451 | A | SWT image/icon cache. Also has system file-icon helpers (`sun.awt.shell` via reflection) |
| [UIHelpers](../xanderView/src/main/java/org/delafer/xanderView/gui/helpers/UIHelpers.java) | 132 | A | Menu-item builder, sleep, misc SWT helpers |
| [Geometry](../xanderView/src/main/java/org/delafer/xanderView/gui/helpers/Geometry.java) | 50 | A | `distanceSquared`/`centerPoint`, the 2 methods kept from the Eclipse JFace `Geometry` copy |
| [BaseSplash](../xanderView/src/main/java/org/delafer/xanderView/gui/splash/BaseSplash.java) | 65 | A | Transient overlay shell base (stacked positions) |
| [SplashWindow](../xanderView/src/main/java/org/delafer/xanderView/gui/splash/SplashWindow.java) | 61 | A | Colored status splash for a `general/State` |
| [SplashText](../xanderView/src/main/java/org/delafer/xanderView/gui/splash/SplashText.java) | 104 | A | Text splash (shows the current copy target) |

### file — playlist, copy service
| Class | LOC | St | Role |
|---|---|---|---|
| [CommonContainer](../xanderView/src/main/java/org/delafer/xanderView/file/CommonContainer.java) | 345 | A | Playlist: picks a reader, sorted list + cursor, next/prev/±10, loop+beep, watcher updates, last-entry restore. State guarded by its monitor (P3.5) |
| [CommonContainerExt](../xanderView/src/main/java/org/delafer/xanderView/file/CommonContainerExt.java) | 98 | A | Adds random mode, only-new mode, start-reached splash |
| [CopyService](../xanderView/src/main/java/org/delafer/xanderView/file/CopyService.java) | 479 | A | One instance per target dir: indexes the target by CRC, `exists()`, queued copy (NIO transfer), name increment, observers |
| [ContentChangeWatcher](../xanderView/src/main/java/org/delafer/xanderView/file/ContentChangeWatcher.java) | 9 | A | Callback interface for reader file events |

### file/entry — image entries, IO, crypto
| Class | LOC | St | Role |
|---|---|---|---|
| [ImageAbstract](../xanderView/src/main/java/org/delafer/xanderView/file/entry/ImageAbstract.java) | 162 | A | Base entry: `ImageType` enum, name/size/CRC, `content()` → `Buf`, equality by size + CRC |
| [ImageFS](../xanderView/src/main/java/org/delafer/xanderView/file/entry/ImageFS.java) | 118 | A | File-system entry (id = path). Wraps `.cry` in `ImageDec` |
| [ImageZip](../xanderView/src/main/java/org/delafer/xanderView/file/entry/ImageZip.java) | 159 | A | Archive entry (id = index). 7-Zip extract callback |
| [ImageDec](../xanderView/src/main/java/org/delafer/xanderView/file/entry/ImageDec.java) | 168 | A | Decorator: decrypts `.cry` and exposes the original name and type |
| [ImageEnc](../xanderView/src/main/java/org/delafer/xanderView/file/entry/ImageEnc.java) | 111 | A | Decorator: encrypts as a copy source (F1) |
| [ImageAimDec](../xanderView/src/main/java/org/delafer/xanderView/file/entry/ImageAimDec.java) | 129 | A | Decorator: first embedded file of an `.aim` container |
| [Buf](../xanderView/src/main/java/org/delafer/xanderView/file/entry/Buf.java) | 56 | A | Closeable ByteBuffer + owner/channel. **Must be closed** |
| [HelperFS](../xanderView/src/main/java/org/delafer/xanderView/file/entry/HelperFS.java) | 104 | A | Reads a file or prefix into `Buf` (mmap). Virtual-dir (`.xav`) checks |
| [Encryptor](../xanderView/src/main/java/org/delafer/xanderView/file/entry/Encryptor.java) | 258 | A | `.cry` encrypt/decrypt (AES-128/ECB/NoPadding, header with name). **Persisted format** |
| [CipherKey](../xanderView/src/main/java/org/delafer/xanderView/file/entry/CipherKey.java) | 80 | A | Key derivation: SHA-256(salt + key file)[0..16] |
| [EncodeName](../xanderView/src/main/java/org/delafer/xanderView/file/entry/EncodeName.java) | 244 | A | File-name sanitizing and transliteration for copies |

### file/readers
| Class | LOC | St | Role |
|---|---|---|---|
| [FileReader](../xanderView/src/main/java/org/delafer/xanderView/file/readers/FileReader.java) | 310 | A | Directory reader: scan, filterCount sampling, WatchService registered before the scan (`com.sun.nio.file` subtree on Windows, top level elsewhere), sub-archive readers |
| [SevenZipReader](../xanderView/src/main/java/org/delafer/xanderView/file/readers/SevenZipReader.java) | 125 | A | Archive reader via 7-Zip-JBinding |
| [ArcTypes](../xanderView/src/main/java/org/delafer/xanderView/file/readers/ArcTypes.java) | 30 | A | Archive extensions: zip, rar, 7z, jzip |

### interfaces, general, hash, sound, process
| Class | LOC | St | Role |
|---|---|---|---|
| [IAbstractReader](../xanderView/src/main/java/org/delafer/xanderView/interfaces/IAbstractReader.java) | 30 | A | Reader contract + `FileEvent` |
| [IFileEntry](../xanderView/src/main/java/org/delafer/xanderView/interfaces/IFileEntry.java) | 18 | A | Entry contract |
| [IImageEntry](../xanderView/src/main/java/org/delafer/xanderView/interfaces/IImageEntry.java) | 37 | A | **Extension → `ImageType` map** |
| [State](../xanderView/src/main/java/org/delafer/xanderView/general/State.java) | 5 | A | Result enum: Success, Ignore, Error, Special1… |
| [Shutdown](../xanderView/src/main/java/org/delafer/xanderView/general/Shutdown.java) | 82 | A | Shutdown hook: persist, wait for tasks, interrupt threads |
| [Hasher](../xanderView/src/main/java/org/delafer/xanderView/hash/Hasher.java) | 69 | A | xxHash64 of the first `HSIZE`=3072 bytes. **Persisted identity** |
| [SoundBeep](../xanderView/src/main/java/org/delafer/xanderView/sound/SoundBeep.java) | 68 | A | Plays `/audio/sound01.wav` |
| [GammaCorrection](../xanderView/src/main/java/org/delafer/xanderView/process/GammaCorrection.java) | 70 | A | LUT gamma on a BufferedImage |

### orientation
| Class | LOC | St | Role |
|---|---|---|---|
| [OrientationCommons](../xanderView/src/main/java/org/delafer/xanderView/orientation/OrientationCommons.java) | 92 | A | `Action`, `Orientation`, `getNewSize` (fit) |
| [CommonRotator](../xanderView/src/main/java/org/delafer/xanderView/orientation/CommonRotator.java) | 65 | A | Orientation state machine + abstract rotate/flip |
| [Rotator2D](../xanderView/src/main/java/org/delafer/xanderView/orientation/Rotator2D.java) | 126 | A | AffineTransform implementation (the one in use) |

### scale — resizers
| Class | LOC | St | Role |
|---|---|---|---|
| [ScaleFactory](../xanderView/src/main/java/org/delafer/xanderView/scale/ScaleFactory.java) | 134 | A | `SCALER` ids 1–12 → `IResizer`. Pick by image size |
| [ScalerHelper](../xanderView/src/main/java/org/delafer/xanderView/scale/ScalerHelper.java) | 132 | A | Parses the `Scaler` config string into size thresholds |
| [IResizer](../xanderView/src/main/java/org/delafer/xanderView/scale/IResizer.java) | 10 | A | `resize(img, w, h)`, `name()` |
| [ResizerBase](../xanderView/src/main/java/org/delafer/xanderView/scale/ResizerBase.java) | 16 | A | Base with filter index |
| [ResizerJava2D](../xanderView/src/main/java/org/delafer/xanderView/scale/ResizerJava2D.java) | 104 | A | AWT nearest/bilinear/bicubic (ids 1–3) |
| [ResizerNewOpenCV](../xanderView/src/main/java/org/delafer/xanderView/scale/ResizerNewOpenCV.java) | 156 | A | OpenCV `Imgproc.resize` (ids 4–7). Loads `opencv_java460` |
| [ResizerNobel](../xanderView/src/main/java/org/delafer/xanderView/scale/ResizerNobel.java) | 78 | A | mortennobel filters (ids 8–11) |

### comparator, filter, common
| Class | LOC | St | Role |
|---|---|---|---|
| [ComparatorFactory](../xanderView/src/main/java/org/delafer/xanderView/comparator/ComparatorFactory.java) | 41 | A | `SortType` → comparator (reversed for desc) |
| [BasicFileComparator](../xanderView/src/main/java/org/delafer/xanderView/comparator/BasicFileComparator.java), [AlphanumComparator](../xanderView/src/main/java/org/delafer/xanderView/comparator/AlphanumComparator.java), [FileSizeComparator](../xanderView/src/main/java/org/delafer/xanderView/comparator/FileSizeComparator.java), [FileDateComparator](../xanderView/src/main/java/org/delafer/xanderView/comparator/FileDateComparator.java), [RandomComparator](../xanderView/src/main/java/org/delafer/xanderView/comparator/RandomComparator.java) | 330 | A | Name / natural / size / date / random (SecureRandom shuffle) |
| [Selector](../xanderView/src/main/java/org/delafer/xanderView/common/Selector.java) | 81 | A | Picks N evenly spaced items (`filterCount`) |
| [CopyLogger](../xanderView/src/main/java/org/delafer/xanderView/common/CopyLogger.java) | 123 | A | Buffered `history_dd_MM_yy.log` writer |
| [INameIncrementer](../xanderView/src/main/java/org/delafer/xanderView/common/INameIncrementer.java), [DefaultNameIncrementer](../xanderView/src/main/java/org/delafer/xanderView/common/DefaultNameIncrementer.java) | 128 | A | Name-conflict suffixing for copies |
| [ImageSize](../xanderView/src/main/java/org/delafer/xanderView/common/ImageSize.java) | 77 | A | Mutable w×h value with scale and swap |
| [FifoMap](../xanderView/src/main/java/org/delafer/xanderView/common/FifoMap.java) | 22 | A | Bounded LinkedHashMap with a dispose hook (font cache) |

### Resources — `xanderView/src/main/resources/`
`xander.properties` (sample config, not loaded from the classpath), `resources/{large,small}_ico.png` (window icons), `audio/sound01.wav` (wrap beep), `simplelogger.properties` (slf4j-simple config, PLAN P1.8).

### Tests — `xanderView/src/test/java/org/delafer/xanderView/` (all scratch `main()`, no JUnit)
`scale/test/TesterSingle` (TurboJPEG/mortennobel quality check), `tests/{Test,TestRead,EncryptorOld}`, `hash/Test` (+`infohash.txt`), `common/ArcFormats`. The benchmark tests of the deleted resizers, the OpenCL kernels and `imageViewer.cmd` were removed (PLAN P2).

## commons-j7 — `commons-j7/src/main/java/net/j7/commons/`
Only the classes used by consumers are listed here; the full usage table is in [DEPENDENCIES.md §3](DEPENDENCIES.md).
| Class | Role |
|---|---|
| [jni/LibraryLoader](../commons-j7/src/main/java/net/j7/commons/jni/LibraryLoader.java) | **Custom native loader** for all 8 natives. Picks the file name per platform (`lib` prefix, `.dylib`/`.jnilib`; P5.2), copies `<location>/libs/<os>/<arch>/<file>` into the AppData cache when missing or changed (exact compare, atomic publish), then `System.load` (JNI) or sets `jna.library.path` (JNA). Windows dependency map: `libavif` → `libgcc_s_seh-1`. Soft-fails. ([ARCHITECTURE §6](ARCHITECTURE.md)) |
| [io/SysInfo](../commons-j7/src/main/java/net/j7/commons/io/SysInfo.java) | OS/arch detection; per-OS app-data dir (`%APPDATA%\xanderView`, `~/.local/share/.xanderView`, `~/Library/Application Support/xanderView`) |
| [io/FilePath](../commons-j7/src/main/java/net/j7/commons/io/FilePath.java) | Fluent path builder (`PathType.ApplicationData`, `forceExists`) |
| [io/FileUtils](../commons-j7/src/main/java/net/j7/commons/io/FileUtils.java) | Extension, base name, path helpers |
| [io/AbstractFileProcessor](../commons-j7/src/main/java/net/j7/commons/io/AbstractFileProcessor.java) | Directory walker (`Recurse` modes, `FileInfo`) used by FileReader |
| [io/TextFileUtils](../commons-j7/src/main/java/net/j7/commons/io/TextFileUtils.java) | Text reader/writer (`_zlst.csv`) |
| [io/ByteBufferFileUtils](../commons-j7/src/main/java/net/j7/commons/io/ByteBufferFileUtils.java) | mmap/ByteBuffer file IO |
| [strings/StringUtils](../commons-j7/src/main/java/net/j7/commons/strings/StringUtils.java), [strings/Args](../commons-j7/src/main/java/net/j7/commons/strings/Args.java) | String helpers; `%1`-style formatting |
| [streams/Streams](../commons-j7/src/main/java/net/j7/commons/streams/Streams.java) | Quiet close |
| [collections/SortedLinkedList](../commons-j7/src/main/java/net/j7/commons/collections/SortedLinkedList.java) | Playlist storage |
| [types/DoubleValue](../commons-j7/src/main/java/net/j7/commons/types/DoubleValue.java), [types/Range](../commons-j7/src/main/java/net/j7/commons/types/Range.java), [types/BitNumber](../commons-j7/src/main/java/net/j7/commons/types/BitNumber.java) | Pair; bit ranges (OrientationStore packing) |
| [base/Equals](../commons-j7/src/main/java/net/j7/commons/base/Equals.java), [base/Numbers](../commons-j7/src/main/java/net/j7/commons/base/Numbers.java), [base/CommonUtils](../commons-j7/src/main/java/net/j7/commons/base/CommonUtils.java) | Null-safe equals, parsing, `nvl` |
| [utils/Metrics](../commons-j7/src/main/java/net/j7/commons/utils/Metrics.java), [utils/BooleanUtils](../commons-j7/src/main/java/net/j7/commons/utils/BooleanUtils.java), [utils/ByteUtils](../commons-j7/src/main/java/net/j7/commons/utils/ByteUtils.java), [utils/RandomUtil](../commons-j7/src/main/java/net/j7/commons/utils/RandomUtil.java) | Timing, bool parsing, byte conversions, randoms |
| [datatypes/CheckSignature](../commons-j7/src/main/java/net/j7/commons/datatypes/CheckSignature.java) | Magic-byte file-type detection |
Resource `commons-j7/src/main/resources/libs.properties` sets `location` = the natives source dir (machine-specific; overridable with `-Dxview.libs.location` / `XVIEW_LIBS_LOCATION`). Logging in commons and the app: SLF4J (`slf4j-simple` binding in the app).

## thirddep-j7 — `thirddep-j7/src/main/java/` + SWT flavor dirs (vendored; see [DEPENDENCIES.md §2](DEPENDENCIES.md))
| Package | Entry points used by the app |
|---|---|
| `org/eclipse/swt`, win32 (`src/swt-win32/java`) | `widgets`, `graphics`, `layout`, `awt/SWT_AWT`, [internal/Library](../thirddep-j7/src/swt-win32/java/org/eclipse/swt/internal/Library.java) (patched loader) |
| `org/eclipse/swt`, gtk/cocoa (`src/swt-stock/java`) | stock SWT 3.135 classes unpacked at build time; [internal/Library](../thirddep-j7/src/swt-stock/java/org/eclipse/swt/internal/Library.java) (upstream 3.135 + `LibraryLoader` patch) |
| `net/sf/sevenzipjbinding` | [SevenZip](../thirddep-j7/src/main/java/net/sf/sevenzipjbinding/SevenZip.java) (patched loader), `IInArchive`, `impl.RandomAccess*Stream` |
| `org/libjpegturbo/turbojpeg` | [TJDecompressor](../thirddep-j7/src/main/java/org/libjpegturbo/turbojpeg/TJDecompressor.java), [TJLoader](../thirddep-j7/src/main/java/org/libjpegturbo/turbojpeg/TJLoader.java) (patched) |
| `com/mortennobel/imagescaling` | `ResampleOp`, `ResampleFilters`, `MultiStepRescaleOp` |
| `com/luciad/imageio/webp` | [WebP](../thirddep-j7/src/main/java/com/luciad/imageio/webp/WebP.java) (patched), SPI only |
| `vavi/imageio/avif`, `vavi/awt/image/{avif,jna/avif}` | AVIF ImageIO SPI (reader and writer), JNA `avif` → `libavif.dll`; [Avif](../thirddep-j7/src/main/java/vavi/awt/image/avif/jna/Avif.java) (patched) |
| `vavi/imageio/jpegxl/jni`, `org/jpeg/jpegxl/wrapper` | JXL ImageIO SPI (reader), JNI `libjxl_jni`; [JpegXLImageReader](../thirddep-j7/src/main/java/vavi/imageio/jpegxl/jni/JpegXLImageReader.java) (patched) |
| External needs | `vavi.util.Debug` (vavi-commons). `vavi.imageio.WrappedImageInputStream` is vendored (PLAN P1.3): [WrappedImageInputStream](../thirddep-j7/src/main/java/vavi/imageio/WrappedImageInputStream.java) |
| Resources | [META-INF/services](../thirddep-j7/src/main/resources/META-INF/services) (ImageIO SPI registry); [SWTMessages*.properties](../thirddep-j7/src/swt-win32/resources/org/eclipse/swt/internal) (win32 flavor; from the removed SWT 4.3 jar) |
