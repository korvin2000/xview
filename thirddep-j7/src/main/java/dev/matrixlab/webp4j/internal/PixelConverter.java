package dev.matrixlab.webp4j.internal;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * xview-patch: vendored from webp4j v2.5.0 (MIT), trimmed to {@link #wrapPixels} (the encoder-side conversions are not
 * vendored).
 */
public final class PixelConverter {

    private PixelConverter() {
    }

    /**
     * Wraps a packed ARGB int[] as a BufferedImage without copying.
     * <p>
     * The returned image shares {@code pixels} as its live backing store and
     * reports {@code TYPE_INT_ARGB} (hasAlpha) or {@code TYPE_INT_RGB} (!hasAlpha).
     *
     * @param pixels Packed ARGB pixels, length {@code width * height}.
     * @param width  Image width.
     * @param height Image height.
     * @param hasAlpha Whether the image should expose an alpha channel.
     * @return A BufferedImage backed directly by {@code pixels}.
     */
    public static BufferedImage wrapPixels(int[] pixels, int width, int height, boolean hasAlpha) {
        if (pixels.length != width * height) {
            throw new IllegalArgumentException(
                    "Pixel array length " + pixels.length + " does not match " + width + "x" + height);
        }

        DirectColorModel colorModel = hasAlpha
                ? new DirectColorModel(32, 0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000)
                : new DirectColorModel(24, 0x00ff0000, 0x0000ff00, 0x000000ff);
        DataBufferInt dataBuffer = new DataBufferInt(pixels, pixels.length);
        WritableRaster raster = Raster.createPackedRaster(
                dataBuffer, width, height, width, colorModel.getMasks(), null);
        return new BufferedImage(colorModel, raster, false, null);
    }
}
