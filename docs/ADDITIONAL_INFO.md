# Additional review findings

> Reviewed 2026-10-12 against checkout `38b05d238391d3026303a309a9c503b55f485af8`, the local dependency cache, and the installed Windows binaries. This is an additive review of [SPEC](../SPEC.md), [INTENT](../INTENT.md), [CLAUDE](../CLAUDE.md), [PLAN](../PLAN.md), the existing `docs`, `.claude/rules`, and `tools`. Existing documents and roadmap statuses have deliberately not been rewritten.
>
> **Evidence labels:** **Observed** means inspected source/artifact or a reported probe result. **Risk** means a failure path supported by source inspection but not reproduced in the GUI. **Proposal** means future work, not an implemented change. An installed artifact is evidence about that artifact, not proof it was built from this checkout.

## 1. Assessment and architectural intent

xanderView has a coherent core: rapid keyboard triage, inexpensive image identity, per-image presentation state, and asynchronous copying. Its complexity comes largely from deployment and dependency ownership. A build needs agreement between Java bindings, ImageIO registrations, native ABI, operating-system prerequisites, and a persistent user cache. Maven coordinates alone cannot describe that agreement.

The three modules are not independent products. `commons-j7` supplies shared infrastructure, notably native loading and paths; `thirddep-j7` adapts third-party implementations; `xanderView` composes the viewer and organizer. Current main-source counts are **100 / 970 / 90 Java files**, respectively, with **3 / 12 / 16 test-source Java files**. These are file counts, not reachability or test-coverage measurements. No `@Test`, `extends TestCase`, or JUnit import was found in those test trees.

The useful design patterns are already small and recognizable:

| Pattern | Actual role and boundary |
|---|---|
| Reader strategy | `IAbstractReader` separates directories from archives; entries still retain reader/native-resource lifetimes. |
| Entry decorators | `ImageDec`, `ImageEnc`, and `ImageAimDec` transform content while forwarding source identity. They also share mutable buffers/state, so they are not immutable values. |
| Resizer strategy and factory | `IResizer` and `ScaleFactory` select implementations by pixel thresholds; configuration is effectively executable dispatch data. |
| ImageIO SPI | Service resources connect WebP, AVIF, and JXL to `ImageIO.read`; packaging is part of functionality. |
| Holder singleton / target multiton | Global configuration and stores, plus one `CopyService` per target string. This is convenient locally but makes isolation, lifecycle, and concurrent work harder. |
| Native adapter | Patched loading hooks delegate selected libraries to `LibraryLoader`; this preserves the owner's compact custom native distribution. |

Preserve those useful boundaries. A framework rewrite or wholesale replacement with self-extracting codec jars would not solve provenance, ABI, or cache problems. Conversely, a common loader does not inherently require vendoring every class of every library: any later reduction must preserve the actual loading hooks and behavior, as the existing PLAN requires.

## 2. Corrections and qualifications to existing documentation

| Existing claim | Additional evidence / correction |
|---|---|
| Every native uses `LibraryLoader`; eight DLLs describe the whole runtime | The eight files describe the owner's explicit cache set. The installed jar also embeds **JNA `jnidispatch.dll`**, and OS runtime libraries are dependencies. Optional xxHash JNI uses another loader. See §4. |
| Natives are copied on first application start | Copying is **lazy per load request**, not an eager eight-file installation transaction. Existing cache files bypass source copying. A warm cache cannot prove first-use behavior. |
| TurboJPEG is a 1.x / 2011 Java API that needs a 3.x migration | The vendored source has 2024 copyright entries and `decompress8`, `setScalingFactor`, and `TJ.PARAM_FASTDCT`. These are already the modern API shape used by the app. Do not execute PLAN P4.2 as though this were an untouched 1.x wrapper; establish the precise upstream revision first. Compare [vendored TJDecompressor](../thirddep-j7/src/main/java/org/libjpegturbo/turbojpeg/TJDecompressor.java) with [upstream 3.0.0 source](https://raw.githubusercontent.com/libjpeg-turbo/libjpeg-turbo/3.0.0/java/org/libjpegturbo/turbojpeg/TJDecompressor.java). |
| The small OpenCV jar is identifiable simply as official OpenCV 4.6.0 with natives removed | Observed jar manifest says **WPILib**, `Implementation-Version: 4.6.0-5`, and `Created-By: WPILib Gradle Build Script`. The local install script assigns different Maven identity, `org.opencv:opencv:4.6.0`. This does not establish how it was minimized or its original download. See §3. |
| The official OpenCV jar is necessarily over 100 MB and bundles all platforms | That describes a particular distribution, not a universal OpenCV Java packaging requirement: the [OpenCV 4.6 Java guide](https://docs.opencv.org/4.6.0/d9/d52/tutorial_java_dev_intro.html) documents separate jar and native paths. The exact large artifact is not identified in the existing docs. Keep the custom jar; record the original distribution and transformation before making a reproducibility claim. |
| `swt.dll` probably includes the missing `swt-awt` / GDI+ symbols | The inspected DLL does not export the expected SWT AWT/GDI+ symbols. The app uses `SWT_AWT.new_Frame`, which reflects into `WEmbeddedFrame`; the reverse `new_Shell` path calls the native AWT loader. Working embedding therefore does not prove merged DLLs. See [SWT_AWT](../thirddep-j7/src/main/java/org/eclipse/swt/awt/SWT_AWT.java), methods `new_Frame` and `new_Shell`. |
| Missing TurboJPEG falls back to ImageIO | [ImageLoader](../xanderView/src/main/java/org/delafer/xanderView/gui/ImageLoader.java), `loadJpegImage`, catches `Exception`, not `UnsatisfiedLinkError`. Missing/incompatible JNI can escape both this fallback and the outer `Exception` handlers. [Java's hierarchy](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/UnsatisfiedLinkError.html) confirms it is a `LinkageError`. |
| Equality compares size, CRC, then names or identifiers | [ImageAbstract.isEquals](../xanderView/src/main/java/org/delafer/xanderView/file/entry/ImageAbstract.java) returns immediately when both cached CRCs exist. Otherwise it falls back to size and name; it does not compare identifiers. Subclasses also specialize equality. |
| `imagedata.bin` stores the orientation ordinal; `.cry` uses `uint16 nameLen` | Both descriptions omit material encoding details. Orientation is **ordinal + 1**; the filename length uses a **signed-short bias**, and padding depends on total encrypted length. Exact rules are in §6. |
| Key handling is entirely raw integers; copy collection access is entirely unsynchronized | `MainWindow` already uses some SWT constants. `CopyService.imageExist` snapshots candidates under the synchronized set's monitor and performs expensive CRC work afterward. Unsynchronized iteration remains elsewhere. Avoid treating roadmap shorthand as proof that no fixes exist. |
| Roughly 30% of app classes / 70% of commons are dead | These remain cleanup estimates, not a measured call graph. SPI, reflection, JNI entry points, wildcard imports, and references inside vendored packages require checking before deletion. |

The app exposes archive **reading/extraction** through `SevenZipReader`; the vendored library's archive-writing APIs do not establish an archive-writing feature in the viewer. Likewise, registered ImageIO writers are not evidence that the UI saves edited pixels or offers format conversion. `.avif` support should not be described as a general raw AV1 video player.

## 3. Build, tooling, and artifact reproducibility

### What was actually verified

The existing [javac helper](../tools/javac-check.sh) passed with the local JDK 21 installation and populated Maven cache:

```text
note: stubbed vavi.imageio.WrappedImageInputStream (vavi-image missing)
note: stripped jiu import from .../ApplConfiguration.java
note: stripped jiu import from .../ScaleFactory.java
OK    commons (184 classes)
OK    thirddep (1454 classes)
OK    xanderView (157 classes)
```

This is **compilation of a temporary adapted source set**, not an unmodified clean Maven build. The helper generates a compile-only replacement for `WrappedImageInputStream`, omits two imports from temporary copies, and uses jars already in `~/.m2`. It does not compile tests, copy resources, shade jars, enumerate SPI providers, load natives, or run the app. Successful output should not be used as release acceptance.

`java`, `mvn`, and `bash` were not on the PowerShell PATH during this review. JDK 21.0.9 was found under `C:/Users/user/.jdks/temurin-21.0.9`; Git Bash was available at `C:/Program Files/Git/bin/bash.exe`. The documented `bash` command therefore assumes a prepared shell. Maven resolution and the historical JitPack HTTP claims in DEPENDENCIES were **not re-executed** here; treat those availability claims as dated observations, not permanent guarantees.

### Missing build guarantees

1. **SPI resource merging.** [thirddep's POM](../thirddep-j7/pom.xml) has an empty transformer list; the [app POM](../xanderView/pom.xml) only transforms the manifest. Neither merges `META-INF/services`. When dependencies contribute the same service file, entries can be lost during shading. The inspected installed jar contains the expected three readers and two writers, so this is a packaging risk, not an observed missing provider in that jar. Maven documents [ServicesResourceTransformer](https://maven.apache.org/plugins/maven-shade-plugin/examples/resource-transformers.html) for merging those resources. Preserve the intentionally disabled JXL-JNA provider when defining the expected provider list.
2. **Nested shading and duplicate classes.** All three modules shade. `thirddep` receives much of its compile classpath as `provided`; the app must supply the runtime closure. A successful thirddep compile does not make its jar independently runnable. Record the final dependency tree, duplicate-class/resource report, and final jar contents after repairing Maven.
3. **Local OpenCV identity.** [register.cmd](../xanderView/opencv/register.cmd) depends on its working directory and installs a local file under an otherwise unverified coordinate. It neither downloads nor authenticates the artifact. A new machine needs the exact jar plus a checksum and provenance, not just the command.
4. **Tracked versus ignored assets.** `git ls-files` reports four files under `commons-j7/target`; no tracked jar/DLL/so, wrapper, or CI YAML was found. [.gitignore](../.gitignore) already ignores `dependency-reduced-pom.xml` and two modules' targets, so PLAN P1.10 is partly an audit/consolidation task. The root docs, rules, tools, and `docs` were themselves untracked at review start. A commit ID alone does not identify that documentation snapshot.
5. **Helper safety and scope.** `javac-check.sh` recursively removes its chosen output directory without containment checking. Pass only a verified disposable path; never use a source, installation, cache, or shared work directory. This review used a new GUID-named directory under the Windows temporary directory. The helper also does not verify the selected compiler's version, does not stop immediately for every missing jar, and writes unquoted source paths into javac argument files; paths containing spaces need a separate portability check.
6. **Doc checker limitations.** [check-docs.sh](../tools/check-docs.sh) checks app-class link labels and relative link destinations. It does not validate fragment anchors, factual accuracy, native inventories, external URLs, class reachability, or complete commons/thirddep coverage. `check-docs: OK` is useful structural validation only.

### Artifact fingerprints observed in this review

| Artifact | Bytes | SHA-256 |
|---|---:|---|
| `xanderView/opencv/opencv-460.jar` | 326,063 | `dd74cfd431ef56eb873ce8f81966c4351cbf7f865b798077a2b060787ac697b6` |
| `C:/Program Files/xview/xanderview.jar` | 8,469,839 | `bac4ab71c446ac139367800cc62676a1b0a0cafa7f9a030d975cc8e32b039e9a` |

The OpenCV jar has no native entries. The installed app manifest declares JDK 23 as the build JDK, while its main class is major version 65 (Java 21). That reconciles the apparent build/runtime mismatch, but checking one class is not proof that every bundled dependency supports Java 21. The manifest has no source commit identifier or `Add-Exports` entry. Do not equate this deployed jar with the newly compiled source.

## 4. Native runtime: beyond the eight-file list

### Loader behavior and failure boundaries

[LibraryLoader](../commons-j7/src/main/java/net/j7/commons/jni/LibraryLoader.java), especially `load`, `load0`, `copyLibrary`, and `getLocations`, establishes these additional contracts:

- The cache is keyed by literal name, without version or checksum. A stale or truncated file is accepted by the existence check. Copying goes directly to the final filename; there is no temporary-file publication, inter-process lock, or integrity validation. **Risk:** interruption or simultaneous first starts can leave a bad persistent file.
- `copyLibrary` catches `IOException` and prints it; `load0` may subsequently call `System.load` on incomplete output. **Missing source** can soft-fail, but **present incompatible/corrupt output** can throw immediately. “All load failures are soft” is too broad.
- `loaded` memoizes both success and failure by name only. A missing-file result remains cached in that JVM. `checkLibrary(name, false)` also records false, so a later `loadLibrary(name)` does not promote that name to a JNI load. The public API returns `void`; callers cannot observe the internal boolean directly.
- That map is an ordinary `HashMap`, and loading/copying is unsynchronized. Independent initialization threads or processes are not serialized by the Holder singleton.
- JNA path properties are assigned globally, replacing previous values. The loader copies only the named library, not its transitive native dependency graph.
- Architecture names are derived from JVM `os.arch`, not a compiler target triple such as `x86_64-pc-windows-msvc`. `aarch64` can pass through as text, but passing through a name is not platform support. OS-specific SWT Java code, native ABI, dependency runtimes, and launch behavior still need validation.

[SysInfo](../commons-j7/src/main/java/net/j7/commons/io/SysInfo.java) also makes the documented app-data paths conditional: Linux tries existing writable bases under `~/.local/share`, then `~/.config`, then home, appending `.xanderView`; it does not honor `XDG_DATA_HOME`/`XDG_CONFIG_HOME`. The macOS fallback uses environment variable `USER` as a path prefix. `LibraryLoader` computes its architecture before assigning `model`, so its intended 64-to-32 adjustment sees null. These are additional portability review points beyond PLAN B7; none was executed on another OS.

These are reasons to add diagnostics and versioned distribution metadata **within** the custom-loader design. They are not reasons to discard it.

### Native loaders outside the custom cache

**Observed:** the deployed jar embeds `com/sun/jna/win32-x86-64/jnidispatch.dll`. JNA must load its own JNI bridge before it can call libavif. [JNA 5.15 Native source](https://github.com/java-native-access/jna/blob/5.15.0/src/com/sun/jna/Native.java) implements its own bootstrap lookup and extraction. The custom `jna.library.path` setting for libavif does not turn that bridge into one of the eight cached files. The actual extraction destination was not measured by launching the app.

**Observed source path:** [Hasher](../xanderView/src/main/java/org/delafer/xanderView/hash/Hasher.java) calls `XXHashFactory.fastestInstance()`. In [lz4-java 1.3.0 XXHashFactory](https://raw.githubusercontent.com/lz4/lz4-java/1.3.0/src/java/net/jpountz/xxhash/XXHashFactory.java), selection can try JNI and fall back to Java. Its [Native helper](https://raw.githubusercontent.com/lz4/lz4-java/1.3.0/src/java/net/jpountz/util/Native.java) extracts a temporary native file independently of `LibraryLoader`. The local dependency jar includes Linux, Darwin, and Windows-named native entries; the deployed app jar contains none of those lz4 natives. Thus a normal Maven repackaging can change extraction and performance behavior even with the same hash algorithm. The actual backend selected at runtime remains unmeasured.

The distribution policy needs an explicit decision about JNA bootstrap and optional xxHash JNI, alongside inactive JOCL and alternative JXL loaders. “All natives go through one loader” is an intent that currently has exceptions.

### Windows binary observations and prerequisites

All eight designated source DLLs and their cache counterparts were present, byte-identical by SHA-256, and PE machine `0x8664` (AMD64). This establishes local inventory consistency, not a successful cold-cache run or Linux/macOS support.

Source inspected: `C:/Program Files/xview/libs/windows/amd64/`. Cache inspected: `C:/Users/user/AppData/Roaming/xanderView/libs/windows/amd64/`.

| DLL | SHA-256 (same in source and cache) |
|---|---|
| `lib7-Zip-JBinding.dll` | `c769642c8a489e8fc078b8387fb9865ac5c5482ce22301a40d00145fa1304b27` |
| `libavif.dll` | `d6dc02fa6028141995d963a795d3a9c5e647946a9698e7869df2101ac6e773f4` |
| `libgcc_s_seh-1.dll` | `d12c1ac03ce7968c91a08b1b9d32f4aacb6d74002aaf40a7f421068067f51d51` |
| `libjxl_jni.dll` | `a7a54377b1f38bb07c22080e7cf8f89310f00fc85b7c9a2c59194e391f358600` |
| `opencv_java460.dll` | `01e27d613c3ab197da42044cc919d37c6c63a23b03eee6149898e3ccb335cabb` |
| `swt.dll` | `3377b359f7bab85d994f3e6da010e1758fdbd00c1a466bc524a11c682c01c769` |
| `turbojpeg.dll` | `4a25e5c9c01070017b5cca9137574da790825e3be9ff60f428b16781d51ff468` |
| `webp-imageio.dll` | `1aa5cb660c18eb4a269a41c7b1d3ac006e1433df18335e75598a6f2c732cc701` |

- **OpenCV imports external runtime components:** `MSVCP140.dll`, `CONCRT140.dll`, `VCRUNTIME140.dll`, `VCRUNTIME140_1.dll`, and Windows Media Foundation DLLs (`MFPlat`, `MF`, `MFReadWrite`). The eight copied files therefore are not a self-contained OS dependency closure. Document the supported Windows edition and runtime provisioning; test a clean machine. Microsoft's [VC++ runtime guidance](https://learn.microsoft.com/en-us/cpp/windows/latest-supported-vc-redist?view=msvc-170) explains matching runtime architecture and toolset requirements.
- **PLAN B9 remains a hypothesis.** Neither the inspected `libavif.dll` nor `lib7-Zip-JBinding.dll` directly imports `libgcc_s_seh-1.dll` in its PE import table. This qualifies the stated MinGW dependency for these particular builds; it does not prove the file is unnecessary, exclude dynamic loads, or authorize deleting it. SevenZip still explicitly requests it after requesting its own DLL.
- **SWT optional features are separate.** The eight-file set lacks `swt-awt` and `swt-gdip`; inspected `swt.dll` does not substantiate the merged-symbol theory. New code using those paths needs corresponding symbol/runtime validation.
- Extra `jxl_jni.dll` and designated `libjxl_jni.dll` in the installation are byte-identical. Keep the filename required by the loader; duplicate identity alone is not authorization to remove owner assets.

Inspection used SHA-256 and Python standard-library parsing of ordinary PE imports/exports. Delay imports and explicit runtime loads were not exhaustively analyzed. JNI can also register methods dynamically, so missing exported names alone do not prove a feature impossible; the SWT source call path is the stronger evidence here. Media Foundation prerequisites are described in Microsoft's [headers and libraries reference](https://learn.microsoft.com/en-us/windows/win32/medfound/media-foundation-headers-and-libraries).

For native release metadata, record OS/arch, filename, SHA-256, upstream revision, patch revision, compiler/toolset, CPU baseline and SIMD options, linked dependency versions, ABI/JNI version, redistribution notices, and a reproducible acquisition/build recipe. None of the inspected repository build scripts establishes that complete chain. A checksum identifies bytes; it does not reconstruct them or demonstrate they are optimized correctly.

## 5. Additional source review findings

The following are source-backed findings, not claims of GUI reproduction. Existing PLAN B1–B10 remain useful; the items here identify omissions or sharpen their scope.

### F1 — Queued copies can deduplicate against the wrong target (high)

In [CopyService](../xanderView/src/main/java/org/delafer/xanderView/file/CopyService.java), `copy` queues work on a particular service, but `copySync` line 209 calls static `exists(entry)`. That resolves `CopyService.instance()` from the **current global target**, while the eventual output path is the original service's `pathTxt`.

Trigger: queue a copy to A, switch to B before the worker performs its existence check, and put the same image only in B. The A job can return `Ignore` despite A not containing the image. The reverse arrangement can create a redundant copy in A. **Proposed regression check:** a blocked worker plus two targets with different inventories; use instance-local dedup for execution while keeping global lookup for current-target UI state.

`CopyObserver.done` also sets `panel.setExist(true)` without checking whether the displayed image or selected target still matches the completed job. Navigating during a copy can mark a different image as already copied.

### F2 — Worker failure and shutdown ordering lack a complete lifecycle (high)

The worker catches `InterruptedException`, but not failures escaping `work.run`. `copySync` catches its own failures; `observer.done` runs outside that catch. For example, a disposed display can cause a callback failure. The current task's `finally` decrements its counter, but the worker can die with `workerStarted` still true and later jobs remaining queued.

[Shutdown](../xanderView/src/main/java/org/delafer/xanderView/general/Shutdown.java), lines 17–34, waits until `openTasks == 0` with no upper bound. A stranded queue can therefore hold shutdown indefinitely. It also flushes copy history **before** waiting for queued copies, without a guaranteed final flush afterward. Config/last-entry state is saved before the wait too. **Proposal:** explicit admission-stop, queue drain, final persistence, and worker closure order; separately test callback failure and copying at exit.

`checkInitialized` waits indefinitely while `initialized == null`; initialization has no failure state. An uncaught failure in the indexing thread before notification can block the caller, including the UI copy handler. Normal caught read errors can instead leave an incomplete index reported as initialized. Documentation should describe both outcomes.

### F3 — Sampling and live watching do not preserve the same target inventory (high)

`CopyService` creates a `FileReader` with `Recurse.Flat`, but [FileReader.read](../xanderView/src/main/java/org/delafer/xanderView/file/readers/FileReader.java) still applies global `filterCount`. Enabling sampling for browsing therefore also samples the initial dedup target index. Existing images omitted from that sample can be copied again.

The watcher unconditionally registers `FILE_TREE`, even for that flat target scan. Subsequent events from descendants can enter an index whose initial scan excluded those descendants. Watch callbacks also bypass the initial scan's sampling and missing-key filtering. Define the intended indexing contract before fixing it: browsing may sample; destination dedup normally needs a complete, consistently scoped inventory.

### F4 — Watchers have correctness and portability gaps beyond synchronization (medium)

[FileReader.initialize](../xanderView/src/main/java/org/delafer/xanderView/file/readers/FileReader.java), lines 185–195, subscribes only to create/delete and ignores `OVERFLOW`. In-place edits do not invalidate cached size/CRC, and lost events do not trigger a rescan. The fixed one-second delay in target create handling is not evidence that a writer has finished.

The `com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE` and archive `ExtendedOpenOption.NOSHARE_WRITE` usages also need per-provider checks on Linux/macOS. A Java compile with exported packages does not prove the filesystem provider supports those options. The [Java WatchService contract](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/WatchService.html) explicitly permits overflow and implementation-dependent behavior. A useful portable baseline is ordinary directory registration plus explicit subtree registration/rescanning where needed.

### F5 — Archive ownership and extraction validity need explicit contracts (high/medium)

[SevenZipReader.read](../xanderView/src/main/java/org/delafer/xanderView/file/readers/SevenZipReader.java) opens a `RandomAccessFile` and, on its always-selected `neu=true` branch, a separate [RandomAccessNioStream](../thirddep-j7/src/main/java/net/sf/sevenzipjbinding/impl/RandomAccessNioStream.java). The second stream is a local variable; `close` explicitly closes only the archive and first file. **Risk:** stream ownership is incomplete; verify handle release and failed-open cleanup before asserting all archive resources are closed.

[ImageZip.MyExtractCallback](../xanderView/src/main/java/org/delafer/xanderView/file/entry/ImageZip.java) ignores `ExtractOperationResult`. It allocates from the declared size, rewinds instead of limiting to actual bytes written, and does not independently verify extraction success. A corrupt archive can yield content whose validity is not checked here. Entry sizes narrow from long to int before direct allocation; there is no documented large-entry or decompression-budget limit. Copying and UI decoding can also use the same archive object concurrently without visible app-level serialization; thread safety needs verification against the exact binding/native version.

Archive `filterCount` samples raw archive indexes before filtering recognized images, so it can return fewer than N images when sampled entries are directories or unsupported files. It is not equivalent to selecting N evenly spaced recognized images.

### F6 — Existing durability protections are useful but incomplete (medium)

`CopyService.nioTransferCopy` already uses `CREATE_NEW`, handles partial channel writes, forces data, closes content, and removes a newly created destination on caught I/O/runtime failure. Preserve these protections. Publication still happens under the final filename while writing, so other readers/watchers can see incomplete content.

[OrientationStore.save](../xanderView/src/main/java/org/delafer/xanderView/gui/config/OrientationStore.java) already writes a temporary file and attempts atomic replacement. It clears `dirty` before writing; failure does not restore it. Its loader treats an incomplete trailing record as EOF and does not validate orientation codes before indexing the enum.

[ApplConfiguration.save](../xanderView/src/main/java/org/delafer/xanderView/gui/config/ApplConfiguration.java) truncates the live properties file directly and clears `unsaved` even after failure. Timer and shutdown saves are not serialized as a transaction. No multi-process ownership/locking is visible for these stores. Backup/recovery and simultaneous-instance behavior are undocumented.

### F7 — File launch and resume parsing need regression cases (medium)

`FileReader.getContainerPath` computes `isDir` before replacing a file with its parent, then returns a path only if the original `isDir` was true. A regular image-file argument therefore yields an empty container path at that method, contrary to SPEC's simple promise to browse its parent. The single entry may still load; the full navigation effect was not exercised.

[CommonContainer](../xanderView/src/main/java/org/delafer/xanderView/file/CommonContainer.java) splits resume data on commas without escaping. Valid filenames containing commas are ambiguous. Drive-letter stripping occurs when **reading** resume state, not as a universal write-time transformation. These details matter for portable collections and migration.

## 6. Persisted format details missing from the current spec

### `.cry`: filename length and block padding

The source chain is [Encryptor](../xanderView/src/main/java/org/delafer/xanderView/file/entry/Encryptor.java) → [ByteUtils](../commons-j7/src/main/java/net/j7/commons/utils/ByteUtils.java), `UIntToByte2` / `Byte2ToUInt`.

For a nonempty payload, let `F` be the image byte length and `N` the UTF-8 basename byte length:

```text
P = (16 - ((F + 2 + N) mod 16)) mod 16
L = N + P
file = int32be(F) | byte(1) |
       AES-128-ECB-NoPadding(encodedLength(L) | nameBytes | P zero bytes | imageBytes)
encodedLength(L) = int16be(clamp(L, 0, 32767) - 32768)
```

The **whole encrypted input** is aligned to 16 bytes; the name alone is not necessarily block-aligned. Stored length includes the zero padding. This is not standard `uint16be(L)`: an isolated Java probe of the actual compiled utility returned `L=6 -> 80 06 -> 6`, whereas ordinary unsigned encoding would be `00 06`. The implementation clamps the encoded length but does not correspondingly truncate an oversized name; define valid input bounds before creating an independent writer.

`decrypt0` reads but does not validate the algorithm byte, does not authenticate the ciphertext, and does not verify that declared plain length matches the returned payload. Empty input follows an early-return path rather than always producing the documented envelope. These are compatibility and validation details, not permission to silently change the legacy format. A future authenticated/versioned format needs a separate migration and legacy reader; present `.cry` as a legacy privacy feature with no integrity guarantee.

### `imagedata.bin`: exact value mapping

Records are big-endian `long key | long packed`, with no header, count, version, or checksum. `OrientationStore.enumOrderNum` stores 0 for absent and **1–8** for the current eight enum values, in the order defined by [OrientationCommons](../xanderView/src/main/java/org/delafer/xanderView/orientation/OrientationCommons.java). Reordering the enum changes persisted meaning.

Each six-bit zoom/pan/gamma field encodes:

| User value | Stored value |
|---|---:|
| 0 | 0 |
| -31 through -1 | 1 through 31 (`v + 32`) |
| +1 through +31 | 32 through 62 (`v + 31`) |

Stored 63 decodes to +32 even though it is outside the documented UI range. Golden files should cover boundaries, absent values, all orientations, truncated records, and invalid ordinals. Merely round-tripping the same implementation would miss a shared encoding mistake.

### Identity is a deliberate approximation

Equal size and identical first 3072 bytes guarantee the same current hash input even if later bytes differ. Dedup may skip distinct files, and those images can share saved view state. This is stronger than the ordinary rare-collision warning for a 64-bit hash. Preserve the existing hash for persisted state; if stricter copy correctness is desired, consider a second full-content verification only after a prefix match, with its performance cost measured.

## 7. Documentation still needed and suggested order

These are proposed additions to the existing roadmap; no PLAN items were changed by this review.

| Priority | Missing evidence or contract | Completion evidence |
|---|---|---|
| First | Reproducible artifact acquisition | Exact sources/checksums for the small OpenCV jar and all custom native builds, plus binding/native compatibility pairs. |
| First | Clean build and packaged-runtime acceptance | Wrapper/toolchain, successful clean-cache Maven build, final jar manifest and SPI inspection, no accidental stock-loader substitution. |
| First | Copy correctness and lifecycle | Two-target queued-copy tests, unsampled target indexing, callback failure handling, drain-before-final-save shutdown. |
| First | Persisted golden fixtures | Owner-approved non-sensitive `.cry` and orientation examples with exact bytes and old-version compatibility assertions. |
| Next | Native cache upgrade/recovery | Identify deployed versus cached versions, close all users of DLLs before replacement, verify hashes, define rollback and interrupted-copy recovery. Do not delete an entire user-data directory to repair a codec. |
| Next | Platform support matrix | Separate compile, startup, embedding, cold-cache load, decode, resize, archive, watcher, copy, persistence, and shutdown results for Windows/Linux/macOS x86_64; aarch64 separately. |
| Next | Native and vendored provenance | Upstream commits, patch inventory, native toolchains/flags, dependency notices and provenance of opaque binaries; no unsupported “safe to delete” conclusions. |
| Next | Runtime failure semantics | Missing codec, corrupt image/archive, wrong key, unsupported algorithm, unwritable destination, no space, stale watcher index, and invalid config behavior. |
| Later | Performance evidence | Cold/warm startup, JPEG DCT scale, codec decode time, render latency, copy throughput, heap/direct memory, and native CPU requirements on identified hardware. |
| Later | Safe cleanup criteria | Measure runtime and intra-module reachability, SPI/reflection/native references, and owner benchmark value before removing legacy trees. |

A compact release manifest beside `libs/<os>/<arch>` would connect the highest-value facts without adding another runtime framework. Keep source revisions, jar/native hashes, expected providers, supported CPU baseline, and launch requirements together. Record unknown build flags or codec revisions as unknown until recovered from actual build records; filenames and age are not sufficient evidence.

## 8. Validation boundaries

Performed: documentation/rules/script review; targeted source review across all modules; POM/resource inspection; tracked-file inventory; isolated fallback compile; a pure-Java probe confirming biased length encoding and the existing 3 MP scaler result (`4`, confirming PLAN B1); installed jar inspection; Windows native/cache hash and PE inspection; primary-source checks linked above. After adding this document and its index link, `tools/check-docs.sh` returned `check-docs: OK`.

Not performed: clean Maven dependency resolution, GUI launch, cold-cache execution, DLL replacement, runtime codec/format corpus testing, archive concurrency reproduction, Linux/macOS/aarch64 execution, or exhaustive vulnerability/license assessment. Neither the deployment nor user configuration/key files were modified. No custom binary was copied, rebuilt, removed, or committed.

The deliverable changes only this document and the index entry. Findings here are review evidence and proposed follow-up work, not fixes or completed roadmap items.
