---
paths:
  - "xanderView/src/main/java/org/delafer/xanderView/hash/**"
  - "xanderView/src/main/java/org/delafer/xanderView/gui/config/**"
  - "xanderView/src/main/java/org/delafer/xanderView/file/entry/Encryptor.java"
  - "xanderView/src/main/java/org/delafer/xanderView/file/entry/CipherKey.java"
  - "xanderView/src/main/java/org/delafer/xanderView/file/entry/ImageAbstract.java"
---
# Persisted formats: existing user data depends on these

| Contract | Code | Breaks if changed |
|---|---|---|
| Image identity: xxHash64 of the first `Hasher.HSIZE`=3072 bytes, seed = file size | `hash/Hasher`, `ImageAbstract.calcCRC` | Every `imagedata.bin` key, and the copy dedup against existing targets |
| `imagedata.bin`: pairs of `long crc, long packed`; the bit `Range`s (0–3, 4–9, 10–15, 16–21, 22–27) and the ±31 bias encoding | `gui/config/OrientationStore` | Saved per-image orientation, zoom, pan and gamma |
| `.cry`: `int32 size, byte alg=1, AES-128/ECB/NoPadding(uint16 nameLen, name padded to 16, data)`; key = SHA-256("z!a3"+keyfile)[0..16] | `file/entry/Encryptor`, `CipherKey` | Every existing encrypted file becomes unreadable |
| `xander.properties` key names and value syntax (`Scaler`, `sort`, `target.folder` `;`-list) | `gui/config/ApplConfiguration` | Existing user configs |

Rules:
- Don't change these unless the task explicitly requires it. If it does, add a versioned migration or read path that keeps old data readable, add a golden-data test (PLAN P3.1), and update `SPEC.md` §6–8.
- Adding new bits (28–63 are free) or new config keys with defaults is safe. Document them in SPEC.
