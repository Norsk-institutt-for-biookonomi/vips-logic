package no.nibio.vips.logic.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.commons.fileupload2.core.FileItem;


public final class UploadedFileStore {

    private UploadedFileStore() {
    }

    public static String requireBasePath(String systemPropertyName) {
        String base = System.getProperty(systemPropertyName);
        if (base == null || base.isBlank()) {
            throw new IllegalStateException(systemPropertyName + " is not set");
        }
        return base;
    }

    public static void write(FileItem item, File destination) throws IOException {
        Path dest = destination.toPath();
        Path dir = dest.getParent();
        if (dir != null) {
            Files.createDirectories(dir);
            if (!Files.isWritable(dir)) {
                throw new IOException("Illustration directory is not writable: " + dir.toAbsolutePath());
            }
        }
        try (InputStream in = item.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
