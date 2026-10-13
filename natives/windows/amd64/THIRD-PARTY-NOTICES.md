# Third-party native libraries: windows-x86_64

Bundle rev 2026.10.2, built by tools/natives/bundle.sh from natives/windows-x86_64.tsv. Each file is an unmodified upstream build (renamed to its load-time name where noted); 'owner build' rows are the maintainer's own builds kept in the tracked tree.

| file | version | license | upstream |
|---|---|---|---|
| `swt-win32-4974r10.dll` | 3.135.0 (4974r10) | EPL-2.0 | https://repo1.maven.org/maven2/org/eclipse/platform/org.eclipse.swt.win32.win32.x86_64/3.135.0/org.eclipse.swt.win32.win32.x86_64-3.135.0.jar (`swt-win32-4974r10.dll`) |
| `swt-osversion-win32-4974r10.dll` | 3.135.0 (4974r10) | EPL-2.0 | https://repo1.maven.org/maven2/org/eclipse/platform/org.eclipse.swt.win32.win32.x86_64/3.135.0/org.eclipse.swt.win32.win32.x86_64-3.135.0.jar (`swt-osversion-win32-4974r10.dll`) |
| `swt-awt-win32-4974r10.dll` | 3.135.0 (4974r10) | EPL-2.0 | https://repo1.maven.org/maven2/org/eclipse/platform/org.eclipse.swt.win32.win32.x86_64/3.135.0/org.eclipse.swt.win32.win32.x86_64-3.135.0.jar (`swt-awt-win32-4974r10.dll`) |
| `swt-gdip-win32-4974r10.dll` | 3.135.0 (4974r10) | EPL-2.0 | https://repo1.maven.org/maven2/org/eclipse/platform/org.eclipse.swt.win32.win32.x86_64/3.135.0/org.eclipse.swt.win32.win32.x86_64-3.135.0.jar (`swt-gdip-win32-4974r10.dll`) |
| `lib7-Zip-JBinding.dll` | 26.03-2.3 (owner's fork korvin2000/sevenzipjbinding, CI release ci4: Windows-amd64-avx2, Clang 23.1.3 -O3 -march=x86-64-v3 -ffast-math -flto, needs AVX2) | LGPL-2.1-or-later+unRAR-restriction | https://github.com/korvin2000/sevenzipjbinding/releases/download/Release-26.03-2.3-ci4/sevenzipjbinding-26.03-2.3-Windows-amd64-avx2.zip (`sevenzipjbinding-26.03-2.3-Windows-amd64/lib/sevenzipjbinding-Windows-amd64.jar!Windows-amd64/lib7-Zip-JBinding.dll`) |
| `turbojpeg.dll` | 3.2.1 (owner's fork korvin2000/libjpeg-turbo, release Release-3.2.1-ci2 Windows-x64-avx2: Clang -O3 -march=x86-64-v3 -ffast-math -flto + PGO, needs AVX2, CTest passed) | BSD-3-Clause+IJG+Zlib | https://github.com/korvin2000/libjpeg-turbo/releases/download/Release-3.2.1-ci2/libjpeg-turbo-3.2.1-Windows-x64-avx2.zip (`libjpeg-turbo-3.2.1-Windows-x64-avx2/bin/libturbojpeg.dll`) |
| `webp4j.dll` | webp4j 2.5.0 JNI library on libwebp 1.6.0 (owner's fork korvin2000/libwebp, release Release-1.6.0-2.5.0-ci2 Windows x64 AVX2: Clang -O3 -march=x86-64-v3 -ffast-math -flto + PGO, needs AVX2, JUnit passed) | MIT(webp4j)+BSD-3-Clause(libwebp) | https://github.com/korvin2000/libwebp/releases/download/Release-1.6.0-2.5.0-ci2/webp4j-windows-x64-avx2.dll (`-`) |
| `libavif.dll` | 1.1.1 (owner build 2024-10-05: MinGW, static dav1d+aom, AVX2-optimized; decode only) | BSD-2-Clause+BSD-2-Clause(dav1d)+BSD-2-Clause+AOM-Patent(aom) | owner build (tracked tree) |
| `libjxl_jni.dll` | libjxl jxl_jni (owner build 2024-09-13: static, AVX2-optimized) | BSD-3-Clause | owner build (tracked tree) |
| `opencv_java460.dll` | 4.6.0 custom minimal build (owner, 2024-10-06; needs the VC++ 2015-2022 redistributable: concrt140.dll) | Apache-2.0 | owner build (tracked tree) |
