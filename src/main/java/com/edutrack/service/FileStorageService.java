package com.edutrack.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDirectory;

    private static final long MAX_FILE_SIZE =
            100L * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "video/mp4"
            );

    public FileStorageService(
            @Value("${edutrack.file.upload-dir}")
            String uploadDirectory
    ) {

        this.uploadDirectory =
                Paths.get(uploadDirectory)
                        .toAbsolutePath()
                        .normalize();

        try {

            Files.createDirectories(
                    this.uploadDirectory
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not create video upload directory",
                    e
            );
        }
    }

    public StoredFile store(
            MultipartFile file
    ) {

        validate(file);

        String originalFileName =
                StringUtils.cleanPath(
                        file.getOriginalFilename()
                );

        String extension =
                getExtension(originalFileName);

        String storedFileName =
                UUID.randomUUID()
                        + extension;

        Path target =
                uploadDirectory
                        .resolve(storedFileName)
                        .normalize();

        if (!target.getParent()
                .equals(uploadDirectory)) {

            throw new IllegalArgumentException(
                    "Invalid filename"
            );
        }

        try (InputStream inputStream =
                     file.getInputStream()) {

            Files.copy(
                    inputStream,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not store video file",
                    e
            );
        }

        return new StoredFile(
                originalFileName,
                storedFileName,
                target.toString(),
                file.getContentType(),
                file.getSize()
        );
    }

    public void delete(String filePath) {

        try {

            Files.deleteIfExists(
                    Paths.get(filePath)
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not delete video file",
                    e
            );
        }
    }

    public Path getPath(String filePath) {

        Path path =
                Paths.get(filePath)
                        .toAbsolutePath()
                        .normalize();

        if (!path.startsWith(uploadDirectory)) {

            throw new IllegalArgumentException(
                    "Invalid video path"
            );
        }

        return path;
    }

    private void validate(
            MultipartFile file
    ) {

        if (file == null
                || file.isEmpty()) {

            throw new IllegalArgumentException(
                    "Please select a video file"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {

            throw new IllegalArgumentException(
                    "Video size must not exceed 100 MB"
            );
        }

        String originalFileName =
                file.getOriginalFilename();

        if (originalFileName == null
                || originalFileName.isBlank()) {

            throw new IllegalArgumentException(
                    "Invalid filename"
            );
        }

        String cleanName =
                StringUtils.cleanPath(
                        originalFileName
                );

        if (cleanName.contains("..")) {

            throw new IllegalArgumentException(
                    "Invalid filename"
            );
        }

        String extension =
                getExtension(cleanName);

        if (!extension.equals(".mp4")) {

            throw new IllegalArgumentException(
                    "Only MP4 video files are allowed"
            );
        }

        String contentType =
                file.getContentType();

        if (!ALLOWED_CONTENT_TYPES
                .contains(contentType)) {

            throw new IllegalArgumentException(
                    "Only video/mp4 files are allowed"
            );
        }
    }

    private String getExtension(
            String fileName
    ) {

        int dot =
                fileName.lastIndexOf('.');

        if (dot == -1) {

            return "";
        }

        return fileName
                .substring(dot)
                .toLowerCase();
    }

    public record StoredFile(
            String originalFileName,
            String storedFileName,
            String filePath,
            String contentType,
            long fileSize
    ) {
    }
}
