package net.woggioni.jshadow.lib;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Random;
import java.util.stream.Stream;

@Execution(ExecutionMode.CONCURRENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class XorStreamTest {
    private static class CaseProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(0, 16, 31, 127, 1000, 1024, 0x1000_000).map(it -> Arguments.of(it));
        }
    }

    @SneakyThrows
    @Timeout(5000)
    @ParameterizedTest(name = "size: \"{0}\"")
    @ArgumentsSource(CaseProvider.class)
    public void test(int size) {
        Random random = new Random(0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[0x10000];
        try(OutputStream outputStream = new DigestOutputStream(new XorOutputStream(baos), md)) {
            int written = 0;
            while (written < size) {
                random.nextBytes(buffer);
                int bytesToWrite = Math.min(size - written, buffer.length);
                outputStream.write(buffer, 0, bytesToWrite);
                written += bytesToWrite;
            }
        }
        byte[] digest = md.digest();
        md.reset();
        try(InputStream inputStream = new DigestInputStream(new XorInputStream(new ByteArrayInputStream(baos.toByteArray())), md)) {
            while (true) {
                int read = inputStream.read(buffer);
                if (read <= 0) break;
            }
        }
        Assertions.assertArrayEquals(digest, md.digest());
    }
}