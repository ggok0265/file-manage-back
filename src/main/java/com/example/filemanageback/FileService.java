package com.example.filemanageback;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;

@Service
public class FileService {

    @Value("${file.base-dir}")
    private String baseDir;

    private Path resolvePath(String subPath) {
        return Paths.get(baseDir).resolve(subPath).normalize();
    }

    public List<Map<String, String>> listFiles(String subPath) {
        File folder = resolvePath(subPath).toFile();
        File[] files = folder.listFiles();
        List<Map<String, String>> result = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                Map<String, String> item = new HashMap<>();
                item.put("name", file.getName());
                item.put("type", file.isDirectory() ? "directory" : "file");
                result.add(item);
            }
        }
        return result;
    }

    public Resource getFile(String filePath) {
        try {
            Path path = resolvePath(filePath);
            return new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException("파일을 찾을 수 없습니다: " + filePath);
        }
    }

    public void saveFile(MultipartFile file, String subDir) {
        try {
            Path target = resolvePath(subDir).resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    public void createDirectory(String subPath) {
        try {
            Path dir = resolvePath(subPath);
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("디렉토리 생성 실패", e);
        }
    }

    public void deletePath(String subPath) {
        Path path = resolvePath(subPath);
        try {
            if (Files.isDirectory(path)) {
                Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } else {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("삭제 실패", e);
        }
    }
}
