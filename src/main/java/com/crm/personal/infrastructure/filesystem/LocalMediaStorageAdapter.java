package com.crm.personal.infrastructure.filesystem;

import com.crm.personal.domain.media.model.StoredMediaFile;
import com.crm.personal.domain.media.port.MediaStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.UUID;

@Component
public class LocalMediaStorageAdapter implements MediaStoragePort {

    private final Path dataDir;

    public LocalMediaStorageAdapter(@Value("${app.data.dir}") String dataDir) {
        this.dataDir = Path.of(dataDir);
    }

    @Override
    public StoredMediaFile store(Path sourceFile) {
        try {
            String originalName = sourceFile.getFileName().toString();
            String extension = extensionOf(originalName);
            String storedName = UUID.randomUUID() + extension;

            YearMonth now = YearMonth.now();
            Path relativePath = Path.of(
                    "media",
                    String.valueOf(now.getYear()),
                    "%02d".formatted(now.getMonthValue()),
                    storedName
            );

            Path target = dataDir.resolve(relativePath).normalize();
            Files.createDirectories(target.getParent());
            Files.copy(sourceFile, target);

            return new StoredMediaFile(
                    relativePath.toString().replace('\\', '/'),
                    originalName,
                    Files.probeContentType(target),
                    Files.size(target)
            );
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar el archivo multimedia", ex);
        }
    }

    @Override
    public Path resolve(String relativePath) {
        Path resolved = dataDir.resolve(relativePath).normalize();
        if (!resolved.startsWith(dataDir.normalize())) {
            throw new SecurityException("Ruta multimedia inválida: " + relativePath);
        }
        return resolved;
    }

    private String extensionOf(String fileName) {
        int index = fileName.lastIndexOf('.');
        return index >= 0 ? fileName.substring(index) : "";
    }
}
