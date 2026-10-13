package dev.matrixlab.webp4j;

import dev.matrixlab.webp4j.internal.NativeWebP;
import dev.matrixlab.webp4j.internal.PixelConverter;
import dev.matrixlab.webp4j.model.VP8StatusCode;
import dev.matrixlab.webp4j.model.WebPBitstreamFeatures;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * xview-patch: vendored from webp4j v2.5.0 (MIT, https://github.com/MrNanko/webp4j), trimmed to the still-image decoder
 * (the encoder, animation, GIF and batch APIs are not vendored). Method bodies are upstream's.
 */
public final class WebPCodec {

    private WebPCodec() {
    }

    /**
     * Checks if WebP support is available on the current platform.
     *
     * @return true if WebP operations are supported on this platform, false otherwise
     */
    public static boolean isAvailable() {
        return NativeWebP.isAvailable();
    }

    /**
     * Retrieves information about a WebP image.
     *
     * @param webPData Byte array containing WebP image data
     * @return int array containing width and height of the image [width, height]
     * @throws IOException If there is an error processing the image
     */
    public static int[] getWebPInfo(byte[] webPData) throws IOException {
        int[] dimensions = new int[2];
        boolean success = NativeWebP.getInfo(webPData, dimensions);

        if (!success) {
            throw new IOException("Failed to retrieve WebP image information.");
        }

        return dimensions;
    }

    /**
     * Decodes a WebP image (stored as a byte array) into an RGB/RGBA BufferedImage.
     *
     * @param webPData The byte array containing the WebP encoded image.
     * @return A BufferedImage representing the decoded RGB/RGBA image.
     * @throws IOException              If an error occurs during retrieval of image info or decoding.
     * @throws IllegalArgumentException If webPData is null or empty.
     */
    public static BufferedImage decodeImage(byte[] webPData) throws IOException {
        if (webPData == null || webPData.length == 0) {
            throw new IllegalArgumentException("The input WebP data cannot be null or empty.");
        }

        WebPBitstreamFeatures features = new WebPBitstreamFeatures();
        int status = NativeWebP.getFeatures(webPData, webPData.length, features);
        VP8StatusCode code = VP8StatusCode.getStatusCode(status);
        if (code != VP8StatusCode.VP8_STATUS_OK) {
            throw new IOException("Failed to get WebP bitstream features, error code: " + code);
        }

        int width = features.getWidth();
        int height = features.getHeight();
        boolean hasAlpha = features.isHasAlpha();

        // Decode straight into the array that will back the returned image.
        int[] pixels = new int[width * height];
        if (!NativeWebP.decodeInto(webPData, pixels, width * 4)) {
            throw new IOException("Failed to decode WebP data.");
        }

        return PixelConverter.wrapPixels(pixels, width, height, hasAlpha);
    }
}
