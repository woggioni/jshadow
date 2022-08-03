package net.woggioni.jshadow.lib;


import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XorInputStream extends FilterInputStream {

    public XorInputStream(InputStream source) {
        super(source);
    }

    private int prev = 0;

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b < 0) return b;
        prev = b ^ prev;
        return prev - 0x80;
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    @Override
    public int read(byte[] buffer, int off, int len) throws IOException {
        int read = 0;
        while (true) {
            int b = super.read();
            if (b < 0) {
                if (read == 0) return -1;
                else break;
            }
            buffer[off + read++] = (byte) ((b ^ prev) - 0x80);
            prev = b;
            if (read == len) break;
        }
        return read;
    }
}
