package coza.opencollab.sakai.cloudcontent;

import java.io.IOException;
import java.io.InputStream;

/**
 * A simple relay stream to read the bytes. retrieved from a stream.
 *
 * @author OpenCollab
 */
public class ByteCountStream extends InputStream{

    private final InputStream inner;
    long bytes = 0;

    /**
     * Constructor setting the real stream.
     */
    ByteCountStream(InputStream in) {
        inner = in;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        int b = inner.read();
        if (b != -1) {
            bytes++;
        }
        return b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte b[]) throws IOException {
        int count = inner.read(b);
        if (count != -1) {
            bytes += count;
        }
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int count = inner.read(b, off, len);
        if (count != -1) {
            bytes += count;
        }
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long skip(long n) throws IOException {
        return inner.skip(n);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int available() throws IOException {
        return inner.available();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        inner.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mark(int readlimit) {
        inner.mark(readlimit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() throws IOException {
        inner.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean markSupported() {
        return inner.markSupported();
    }
}
