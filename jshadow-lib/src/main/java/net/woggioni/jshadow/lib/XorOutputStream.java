package net.woggioni.jshadow.lib;


import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class XorOutputStream extends FilterOutputStream {

    private int prev = 0;

    public XorOutputStream(OutputStream destination) {
        super(destination);
    }

    @Override
    public void write(int c) throws IOException {
        int b = (c + 0x80) ^ prev;
        super.write(b);
        prev = b;
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    @Override
    public void write(byte[] buffer, int off, int len) throws IOException {
        int written = 0;
        while (true) {
            int b = (buffer[written] + 0x80) ^ prev;
            super.write(b);
            prev = b;
            ++written;
            if (written == len) break;
        }
    }
}