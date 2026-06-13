package com.unimas.library.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Stores uploaded images on the local file system under ./uploads/{subdir}
 * and returns the public URL (/uploads/...) served by WebConfig.
 */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_BYTES = 5 * 1024 * 1024; // 5 MB

    private final Path root;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /** @return public URL like /uploads/covers/uuid.png, or null if file empty. */
    public String store(MultipartFile file, String subdir) {
        if (file == null || file.isEmpty()) return null;
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalStateException("Image is too large (max 5 MB).");
        }
        if (!ALLOWED.contains(file.getContentType())) {
            throw new IllegalStateException("Only JPG, PNG, WEBP or GIF images are allowed.");
        }
        try {
            Path dir = root.resolve(subdir);
            Files.createDirectories(dir);
            String original = StringUtils.cleanPath(
                    file.getOriginalFilename() == null ? "img" : file.getOriginalFilename());
            String ext = original.contains(".")
                    ? original.substring(original.lastIndexOf('.')) : "";
            String name = UUID.randomUUID() + ext;
            Files.copy(file.getInputStream(), dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + subdir + "/" + name;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store the uploaded image.", e);
        }
    }
}
