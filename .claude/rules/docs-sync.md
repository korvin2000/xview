---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/src/main/resources/**"
---
# Doc sync: path → doc section to update (same change as the code)

App paths are relative to `xanderView/src/main/java/org/delafer/xanderView/`.

| If you changed… | Update |
|---|---|
| Any class added, deleted, renamed, or a changed role or status (A/D/T/X) | `docs/SOURCE_MAP.md` row (keep the link and LOC roughly right) |
| `gui/MainWindow.bindKeyEvent`, the menu accelerators | `SPEC.md` §5 key table |
| `interfaces/IImageEntry.types`, `ImageAbstract.ImageType`, `file/readers/ArcTypes`, `gui/ImageLoader` | `SPEC.md` §2; `docs/ARCHITECTURE.md` §5 if the pipeline changed |
| `file/CommonContainer*`, `comparator/*` | `SPEC.md` §3 |
| `gui/ImageCanvas`, `orientation/*`, `process/*`, `scale/*` | `SPEC.md` §4 / §8 (`Scaler` ids); `ARCHITECTURE.md` §5 |
| `file/CopyService`, `common/CopyLogger`, `common/*NameIncrementer`, `file/entry/{Encryptor,CipherKey}` | `SPEC.md` §6 |
| `gui/config/ApplConfiguration` keys or defaults; any file written to disk | `SPEC.md` §7 / §8 |
| Threads, executors, `asyncExec`/`invokeLater`, `Shutdown` | `docs/ARCHITECTURE.md` §2–3 |
| `IAbstractReader`, `ImageAbstract` hierarchy, `Buf` | `docs/ARCHITECTURE.md` §4 |
| Any `pom.xml`, repository, dependency, plugin | `docs/DEPENDENCIES.md` §1; `docs/BUILD.md` if commands or blockers changed; `tools/javac-check.sh` classpath |
| `LibraryLoader`, `libs.properties`, any `loadLibrary` call, DLL names | `docs/ARCHITECTURE.md` §6; `docs/DEPENDENCIES.md` §4 |
| Vendored lib patched, replaced or removed | `docs/DEPENDENCIES.md` §2; `docs/SOURCE_MAP.md` thirddep table |
| Parent `swt-*` profiles, `thirddep-j7/src/swt-stock`, `gui/PlatformLaunch` | `docs/BUILD.md` (SWT flavors); `docs/DEPENDENCIES.md` §2/§4; `docs/ARCHITECTURE.md` §1/§6; `PLAN.md` P5 |
| Finished or obsoleted a PLAN item; found new debt or a bug | `PLAN.md` (tick it, or add it with evidence `path:line`) |
| Fixed a PLAN blocker that `tools/javac-check.sh` works around | Remove that workaround from the script and update `docs/BUILD.md` |

Finish with `bash tools/check-docs.sh` (it must print `check-docs: OK`). If the change alters a durable, non-obvious fact recorded in auto-memory, update that memory file too.
