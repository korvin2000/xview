package dev.matrixlab.webp4j.internal;

import dev.matrixlab.webp4j.model.WebPBitstreamFeatures;
import net.j7.commons.jni.LibraryLoader;

/**
 * xview-patch: vendored from webp4j v2.5.0 (MIT), trimmed to the natives the decoder uses (the JNI library exports the
 * full set; declared natives bind lazily, so the omitted ones are simply never resolved). The model classes that the
 * library's JNI_OnLoad caches (WebPBitstreamFeatures, AnimationInfo, AnimatedWebPData) are vendored verbatim, or
 * System.load fails. The library is loaded through {@link LibraryLoader} (load name {@code webp4j}) instead of upstream's
 * temp-file extraction.
 */
public class NativeWebP {

    /**
     * Holds the cause of native library unavailability.
     * If null, the library loaded successfully and all native methods are available.
     * If not null, contains the exception that prevented loading.
     */
    private static final Throwable UNAVAILABILITY_CAUSE;

    private NativeWebP() {
        throw new AssertionError("Cannot instantiate utility class.");
    }

    /**
     * Simple native method for smoke testing.
     * Returns the libwebp version number to verify JNI bindings work correctly.
     *
     * @return WebP library version number (e.g., 0x010300 for version 1.3.0)
     */
    private static native int getLibWebPVersion();

    /**
     * int WebPGetInfo(const uint8_t* data, size_t data_size, int* width, int* height);
     */
    public static native boolean getInfo(byte[] data, int[] dimensions);

    /**
     * VP8StatusCode WebPGetFeatures(const uint8_t* data, size_t data_size, WebPBitstreamFeatures* features);
     */
    public static native int getFeatures(byte[] data, int dataSize, WebPBitstreamFeatures features);

    /**
     * Decodes a WebP bitstream into packed ARGB pixels (0xAARRGGBB ints).
     * Both arrays stay pinned for the full duration of the decode, which can
     * delay GC for very large images - the accepted cost of zero-copy.
     *
     * @param data         WebP bitstream
     * @param output       Destination, length must equal width * height
     * @param outputStride Row stride in BYTES (width * 4)
     * @return true on success
     */
    public static native boolean decodeInto(byte[] data, int[] output, int outputStride);

    /**
     * Checks if the native library has been successfully loaded.
     *
     * @return true if the native library is loaded and available, false otherwise
     */
    public static boolean isAvailable() {
        return UNAVAILABILITY_CAUSE == null;
    }

    /**
     * Returns the cause of unavailability if the native library failed to load.
     *
     * @return The Throwable that caused loading to fail, or null if library loaded successfully
     */
    public static Throwable unavailabilityCause() {
        return UNAVAILABILITY_CAUSE;
    }

    // Static initializer: attempt to load the native library when class is loaded
    static {
        Throwable cause = null;
        try {
            LibraryLoader.loadLibrary("webp4j"); // xview-patch: was NativeLibraryLoader.loadLibrary() (jar extraction)

            // Perform smoke test - verify JNI bindings work correctly
            // This ensures not just that the library loaded, but that native methods are callable
            int version = getLibWebPVersion();
            if (version <= 0) {
                throw new IllegalStateException("Invalid libwebp version: " + version);
            }
        } catch (LinkageError | Exception e) {
            cause = e;
        }
        UNAVAILABILITY_CAUSE = cause;
    }

}
