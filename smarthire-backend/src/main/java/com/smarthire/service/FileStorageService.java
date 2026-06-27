package com.smarthire.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * FileStorageService — Handles saving uploaded files to the server's filesystem.
 *
 * When a candidate uploads a resume PDF:
 *   1. This service creates a unique filename (to prevent collisions)
 *   2. Saves the file to the configured upload directory
 *   3. Returns the file path so it can be stored in the database
 *
 * File naming strategy:
 *   Original: "John_Resume.pdf"
 *   Stored as: "a1b2c3d4-John_Resume.pdf"
 *   The UUID prefix ensures no two files ever overwrite each other,
 *   even if two candidates upload files with the same name.
 *
 * Security considerations:
 *   - We validate the file is not empty
 *   - We validate it's a PDF file (by content type)
 *   - We sanitize the filename to prevent path traversal attacks
 *   - The upload directory is created automatically if it doesn't exist
 */
@Service
@Slf4j
public class FileStorageService {

    private final Path uploadDir;

    /**
     * Constructor reads the upload directory from application.yml.
     * @Value("${app.file.upload-dir}") injects the property value.
     */
    public FileStorageService(@Value("${app.file.upload-dir}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * @PostConstruct runs after the bean is created.
     * Creates the upload directory if it doesn't exist.
     */
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadDir);
            log.info("Upload directory initialized at: {}", uploadDir);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, ex);
        }
    }

    /**
     * Store an uploaded file to disk.
     *
     * @param file the uploaded multipart file
     * @return the absolute path where the file was saved
     * @throws RuntimeException if the file is empty, not a PDF, or saving fails
     */
    public String storeFile(MultipartFile file) {
        // Validate: file must not be empty
        if (file.isEmpty()) {
            throw new RuntimeException("Cannot upload an empty file");
        }

        // Validate: file must be a PDF
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new RuntimeException("Only PDF files are allowed. Received: " + contentType);
        }

        try {
            // Get the original filename and sanitize it
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                originalFilename = "resume.pdf";
            }

            // Remove path separators to prevent path traversal attacks
            // "../../../etc/passwd" becomes "etc_passwd"
            String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

            // Add UUID prefix for uniqueness
            String uniqueFilename = UUID.randomUUID().toString().substring(0, 8) + "-" + sanitizedFilename;

            // Resolve the target path and save the file
            Path targetPath = uploadDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully: {}", targetPath);
            return targetPath.toString();

        } catch (IOException ex) {
            log.error("Failed to store file: {}", ex.getMessage());
            throw new RuntimeException("Failed to store file: " + ex.getMessage(), ex);
        }
    }

    /**
     * Delete a file from disk.
     * Used when a candidate re-uploads a resume (we delete the old one first).
     *
     * @param filePath the path to the file to delete
     */
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("File deleted: {}", filePath);
            }
        } catch (IOException ex) {
            log.warn("Could not delete file: {} - {}", filePath, ex.getMessage());
        }
    }
}
