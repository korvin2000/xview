/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio;

import java.io.IOException;
import java.io.InputStream;
import javax.imageio.stream.ImageInputStream;


/**
 * WrappedImageInputStream.
 *
 * xview-patch: vendored verbatim from umjammer/vavi-image tag 1.0.11
 * (src/main/java/vavi/imageio/WrappedImageInputStream.java) because the jitpack artifact
 * com.github.umjammer:vavi-image:1.0.11 is no longer resolvable; only this class was used.
 * Local change: {@link #available()} no longer returns negative values (see below).
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 021115 nsano initial version <br>
 */
public class WrappedImageInputStream extends InputStream {

    /** */
    private ImageInputStream is;

    /** */
    public WrappedImageInputStream(ImageInputStream is) {
        this.is = is;
    }

    @Override
    public int available() throws IOException {
        // xview-patch: upstream returned (int) (is.length() - is.getStreamPosition()), which is negative
        // when the length is unknown (-1) and can overflow; InputStream requires a value >= 0.
        long length = is.length();
        if (length < 0) {
            return 0;
        }
        long remaining = length - is.getStreamPosition();
        return (int) Math.max(0, Math.min(remaining, Integer.MAX_VALUE));
    }

    @Override
    public void close() throws IOException {
        is.close();
    }

    /* TODO */
    @Override
    public void mark(int readlimit) {
        is.mark();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read() throws IOException {
        return is.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return is.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return is.read(b, off, len);
    }

    @Override
    public void reset() throws IOException {
        is.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return is.skipBytes(n);
    }
}

/* */
