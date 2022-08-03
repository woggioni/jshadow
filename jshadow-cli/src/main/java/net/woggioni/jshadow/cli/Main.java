package net.woggioni.jshadow.cli;

import lombok.SneakyThrows;
import net.woggioni.jshadow.lib.XorInputStream;
import net.woggioni.jshadow.lib.XorOutputStream;
import picocli.CommandLine;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

class VersionProvider implements CommandLine.IVersionProvider {
    private final String version;

    @SneakyThrows
    VersionProvider() {
        Enumeration<URL> it = getClass().getClassLoader().getResources(JarFile.MANIFEST_NAME);

        while(it.hasMoreElements()) {
            URL manifestURL = it.nextElement();

            Manifest mf = new Manifest();
            try(InputStream is = manifestURL.openStream()) {
                mf.read(is);
            }
            Attributes mainAttributes = mf.getMainAttributes();
            if(Objects.equals("jshadow", mainAttributes.getValue(Name.SPECIFICATION_TITLE))) {
                version = mainAttributes.getValue(Name.SPECIFICATION_VERSION);
                return;
            }
        }
        throw new RuntimeException("Version information not found in manifest");
    }

    @Override
    public String[] getVersion() {
        return new String[] { version };
    }
}

@CommandLine.Command(name = "jshadow", description = "obfuscate/deobfuscate files", mixinStandardHelpOptions = true, versionProvider = VersionProvider.class)
public class Main implements Runnable {

    @CommandLine.Option(names = {"-i", "--input-file"}, description = "Path to the input file (if not provided defaults to stdin)")
    private Path inputFile;

    @CommandLine.Option(names = {"-o", "--output"},
            description = "Write output to the provided file path (if not provided defaults to stdout)")
    private Path outputFile;

    @CommandLine.Option(names = {"-d", "--de-obfuscate"}, description = "Remove obfuscation from the input")
    private boolean deobfuscate = false;

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new Main());
        System.exit(commandLine.execute(args));
    }

    @SneakyThrows
    private static InputStream readFile(Path path) {
        return Files.newInputStream(path);
    }

    @SneakyThrows
    private static OutputStream writeFile(Path path) {
        return Files.newOutputStream(path);
    }


    private InputStream openInputStream() {
        InputStream result = Optional.ofNullable(inputFile).map(Main::readFile).orElse(System.in);
        return deobfuscate ? new XorInputStream(result) : result;
    }

    private OutputStream openOutputStream() {
        OutputStream result = Optional.ofNullable(outputFile).map(Main::writeFile).orElse(System.out);
        return deobfuscate ? result : new XorOutputStream(result);
    }


    @Override
    @SneakyThrows
    public void run() {
        try(InputStream inputStream = openInputStream()) {
            try(OutputStream outputStream = openOutputStream()) {
                byte[] buffer = new byte[0x10_000];
                while (true) {
                    int read = inputStream.read(buffer);
                    if(read < 0) break;
                    outputStream.write(buffer, 0, read);
                }
            }
        }
    }
}