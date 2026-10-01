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
        Path dest = destination.toPath().toAbsolutePath();
        Path dir = dest.getParent();
        try {
            if (dir != null) {
                Files.createDirectories(dir);
            }
            try (InputStream in = item.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IOException(writeFailureMessage(dest, dir, e), e);
        }
    }

    static String writeFailureMessage(Path dest, Path dir, IOException cause) {
        boolean dirWritable = false;
        try {
            dirWritable = dir != null && Files.isWritable(dir);
        } catch (Exception ignored) {

        }
        return "Could not store illustration at " + dest
                + " (user=" + System.getProperty("user.name")
                + ", dir=" + dir
                + ", dirWritable=" + dirWritable
                + "): " + cause.getMessage();
    }
}
