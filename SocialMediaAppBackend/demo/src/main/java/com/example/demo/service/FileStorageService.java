package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path uploadDir = Paths.get("uploads");

    public FileStorageService() {
        try {
            Files.createDirectories(uploadDir);
        }
        catch (Exception e) {
            throw new RuntimeException("Couldn't create upload directory ", e);
        }
    }

    public String store(MultipartFile file) {
        if(file == null || file.isEmpty())
            return null;

        try {
            String originalName = file.getOriginalFilename();
            String extension = "";

            if(originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID() + extension;

            Path target = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + fileName;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to store file ", e);
        }
    }

    public void delete(String fileUrl) {
        if(fileUrl == null || !fileUrl.startsWith("/uploads/"))
            return;

        try {
            String fileName = fileUrl.replace("/uploads/", "");
            Path filePath = uploadDir.resolve(fileName);
            Files.deleteIfExists(filePath);
        }
        catch (IOException e) {
            System.err.println("Failed to delete file " + e.getMessage());
        }

    }
}
