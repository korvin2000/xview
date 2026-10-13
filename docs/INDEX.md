# Index — documentation map and task router

> Start here. Open only the row you need. App code paths are relative to `xanderView/src/main/java/org/delafer/xanderView/`.

## Documentation map
```
CLAUDE.md ─ always loaded: commands, big picture, rules, doc-sync
├─ INTENT.md ─ why: goals, values, non-goals, open questions
├─ SPEC.md ─ what: formats §2, navigation §3, view §4, keys §5, copy/crypto §6, files §7, config §8
├─ PLAN.md ─ status, roadmap P0–P5 (P5 = cross-platform), known bugs B1–B10
└─ docs/
   ├─ INDEX.md ─ this file
   ├─ ADDITIONAL_INFO.md ─ independent review, doc corrections, native fingerprints, source findings, validation gaps
   ├─ SOURCE_MAP.md ─ every app class (status A/D/T/X), used commons classes, thirddep entry points
   ├─ ARCHITECTURE.md ─ modules §1, startup §2, threads §3, data model §4, pipeline §5, natives §6, extension points §7, patterns §8
   ├─ DEPENDENCIES.md ─ native design §0, Maven deps §1, vendored libs §2, commons usage §3, natives + platform matrix §4, vavi research §5
   ├─ NATIVES.md ─ natives: what the DLLs really are, 6-platform sources, bundles + manifests, loader modes, owner workflows, open decisions (P7)
   └─ BUILD.md ─ why hard, toolchain, javac-check, Maven blockers, deploy layout, run, tests
.claude/rules/ ─ path-scoped rules, auto-loaded for matching files (docs-sync, thirddep, commons, persisted-formats)
tools/ ─ javac-check.sh (compile without Maven), check-docs.sh (doc drift)
```

## Task router
| I want to… | Read first | Then edit |
|---|---|---|
| Review additional findings, documentation corrections, native fingerprints, and remaining verification gaps | [ADDITIONAL_INFO.md](ADDITIONAL_INFO.md) | — |
| Build or compile-check | [BUILD.md](BUILD.md) | — |
| Understand the runtime flow | [ARCHITECTURE.md §2–5](ARCHITECTURE.md) | — |
| Change or add a key binding | [SPEC §5](../SPEC.md) | `gui/MainWindow.bindKeyEvent` |
| Support a new image format | [ARCHITECTURE §7](ARCHITECTURE.md) | `interfaces/IImageEntry`, `file/entry/ImageAbstract`, `gui/ImageLoader`, thirddep `META-INF/services` |
| Fix decoding of a format | [DEPENDENCIES §2](DEPENDENCIES.md) | `gui/ImageLoader`; JPEG → `codec/TurboJpeg` (FFM); WebP → `dev/matrixlab/webp4j` (thirddep); AVIF/JXL → thirddep SPI |
| Change scaling quality or speed | [SPEC §8 `Scaler`](../SPEC.md) | `scale/ScaleFactory`, `scale/ScalerHelper`, `scale/Resizer*` |
| Change zoom/pan/gamma/rotation | [SPEC §4](../SPEC.md) | `gui/ImageCanvas`, `orientation/*`, `process/GammaCorrection` |
| Change what is saved per image | [SPEC §7](../SPEC.md) (persisted!) | `gui/config/OrientationStore` |
| Change the copy / dedup behavior | [SPEC §6](../SPEC.md) | `file/CopyService`, `common/*NameIncrementer`, `common/CopyLogger` |
| Touch encryption (`.cry`) | [SPEC §6](../SPEC.md) (persisted!) | `file/entry/{Encryptor,CipherKey,ImageDec,ImageEnc}` |
| Change navigation, sorting, modes | [SPEC §3](../SPEC.md) | `file/CommonContainer(Ext)`, `comparator/*` |
| Read directories or archives | [ARCHITECTURE §4](ARCHITECTURE.md) | `file/readers/*`, `file/entry/{ImageFS,ImageZip,HelperFS}` |
| Add a config option | [SPEC §8](../SPEC.md) | `gui/config/ApplConfiguration` |
| Fix a native DLL load failure | [NATIVES §4](NATIVES.md), [ARCHITECTURE §6](ARCHITECTURE.md), [DEPENDENCIES §4](DEPENDENCIES.md#natives) | `java -jar xanderview.jar --check-natives`; `commons-j7/.../jni/LibraryLoader`; the cache `%APPDATA%\xanderView\libs\<os>\<arch>\[<bundle12>\]` |
| Add or update a native library, build the bundles | [NATIVES.md](NATIVES.md), [natives/README.md](../natives/README.md) | `natives/<platform>.tsv`, `tools/natives/bundle.sh`, `check-binaries.sh`, the pom's `xview.natives.*` properties |
| Build the per-platform dist zips | [BUILD natives bundles](BUILD.md), [NATIVES §3](NATIVES.md) | `xanderView/src/assembly/dist.xml`, `src/dist/`, `.github/workflows/build.yml` |
| Port to Linux/macOS or aarch64 | [NATIVES §2](NATIVES.md), [PLAN P5/P7](../PLAN.md), [DEPENDENCIES §4 matrix](DEPENDENCIES.md#natives) | parent `swt-*` profiles, `thirddep-j7/src/swt-{win32,stock}`, `gui/PlatformLaunch`, `LibraryLoader`, `SysInfo`, `natives/*.tsv` |
| Fix the vavi / jitpack dependencies | [DEPENDENCIES §5](DEPENDENCIES.md) | `thirddep-j7/pom.xml` |
| Window, fullscreen, splash UI | [SOURCE_MAP gui/*](SOURCE_MAP.md) | `gui/helpers/MultiShell`, `gui/splash/*` |
| Remove dead code | [PLAN P2](../PLAN.md), [SOURCE_MAP](SOURCE_MAP.md) status D/X | grep for references first |
| Replace a vendored library | [DEPENDENCIES §2](DEPENDENCIES.md), [PLAN P4](../PLAN.md) | `thirddep-j7/pom.xml`, parent `pom.xml` |
| Shutdown or persistence timing | [ARCHITECTURE §2–3](ARCHITECTURE.md) | `general/Shutdown`, `ApplConfiguration` timer |

## Search tips (token savers)
- The app code is small (about 90 files, about 8k LOC). `thirddep-j7` holds about 1000 vendored files, so **exclude it from searches** unless the task is about a vendored lib: use `Grep` with `path: xanderView/src` or `glob: "!thirddep-j7/**"`.
- Key handling is one switch on `SWT.*` constants and character literals (`SWT.F1`, `'s'`); the key table is [SPEC §5](../SPEC.md).
- `CRC` in the code means the xxHash64 prefix hash, not CRC32.
- Many classes have a scratch `main()`; they are not entry points. The only real one is `gui/MainEntryPoint`.
