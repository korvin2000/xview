# INTENT — why xanderView exists

> Derived from the code, its comments and the config defaults on 2026-10-12. Lines marked *(inferred)* should be confirmed with the owner.

## Purpose
xanderView is used to **triage large image collections quickly**. You open a folder or an archive, step through it full screen with the keyboard, and **copy the keepers into one of several target folders with a single key**. The app remembers how each image should be shown: rotation, zoom, pan and gamma.

## Users and platforms
- A single power user: the author. They use the keyboard only, and their collections hold thousands of images, some of them in archives or encrypted. *(inferred: personal tool, not a product)*
- **Platforms (owner):**
  - Windows, Linux and macOS (Apple Darwin) on **x86_64 (required)**, with **aarch64 (optional, preferred)**.
  - Only Windows x64 is implemented today.

## Core values (the order matters when they conflict)
1. **Speed of the next image.** Some design choices trade completeness for speed:
   - TurboJPEG decodes with DCT-domain downscaling.
   - Files are read through mmap.
   - The content hash uses only the first 3072 bytes (`Hasher` javadoc calls this deliberate).
   - Each image-size class gets its own resizer.
2. **Zero-friction keyboard flow.** There are no dialogs in the main loop. Status shows as transient splash overlays and a caption, and copy runs asynchronously.
3. **Correct presentation without effort.** The app auto-rotates when the image's orientation doesn't match the screen, fits the image to the screen, and lets you save a per-image override (`S`).
4. **Never copy the same image twice.** Target folders are indexed by content hash. Images already copied get a red `*` caption and can be skipped with the "only new" mode.
5. **Privacy option.** `.cry` files are AES-encrypted images with an embedded original name. They are readable only when the key file exists. *(inferred: protects sensitive collections at rest)*

## Philosophy and patterns
- A pragmatic monolith: singletons (`Holder` idiom), static helpers, and a few interfaces where variation is real: `IAbstractReader`, `IResizer`, `INameIncrementer`, the `ImageAbstract` decorators.
- **Custom native stack (owner's design):**
  - Format support and speed come from native codecs: TurboJPEG, 7-Zip, libwebp, libavif (JNA), libjxl (JNI) and OpenCV, plus a custom SWT build.
  - These are **custom builds optimized for performance**. Some are compiled and copied by hand per OS/arch.
  - Stock jars ship every platform's natives and extract them themselves. Instead, the native-using libraries are **vendored and patched**, so one loader (`LibraryLoader`) keeps the right natives in a persistent per-user directory (copied on first use, re-copied only when changed) (`…/xanderView/libs/<os>/<arch>/`) and loads them from there.
  - For the same reason, OpenCV is a **minified jar** (about 320 KB instead of over 100 MB) with a custom `opencv_java460` native.
- Custom ImageIO SPIs plug the codecs into the standard `ImageIO.read` path.
- Configuration is a flat `.properties` file. Persistence is compact binary (`imagedata.bin`) rather than a database.

## Non-goals
- Image editing or saving modified pixels. Gamma, zoom and rotation are view-only.
- Library or catalog management: tags, a metadata DB, EXIF browsing.
- Multi-user use, networking, or cloud.
- Stock "batteries-included" native jars (multi-OS self-extracting) and the full 100 MB+ OpenCV distribution.

## Modernization intent (what "done" looks like)
- Reproducible build on a clean machine: `mvnw verify`, with the custom natives and the minified OpenCV jar obtainable from a documented, versioned source.
- The custom native-loading design is kept. Vendored code is reduced only where that doesn't break it: pure-Java libraries move to Maven artifacts, and native-backed ones stay patched with every patch tracked.
- Cross-platform: the same app runs on Windows, Linux and macOS x86_64 (aarch64 optional), each with its own `libs/<os>/<arch>/` native set.
- Dead code and trash removed. Tests guard the persisted formats and navigation logic.
- **Behavior and on-disk formats unchanged** unless a migration is provided.

## Open questions for the owner
- How are the custom natives built (toolchain, flags, upstream versions)? This should be documented for reproducibility (PLAN P1.9).
- Are `.aim`, `.xav` and `jzip` formats still in use?
- ~~Should `scale/deprecated`, `scale/others` and the OpenCL rotators be kept for benchmarking, or deleted?~~ Deleted (owner decision, 2026-10-12, PLAN P2.2).
- Natives source for reproducible builds: Git LFS in-repo, or a release archive plus a SHA-256 manifest (PLAN P1.9)?
