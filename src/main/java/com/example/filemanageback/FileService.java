package com.example.filemanageback;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;

@Service
public class FileService {

    @Value("${file.base-dir}")
    private String baseDir;

    // 경로 생성 및 정규화
    private Path resolvePath(String subPath) {
        return Paths.get(baseDir).resolve(subPath).normalize();
    }

    // 파일 및 디렉토리 목록 반환
    public List<Map<String, String>> listFiles(String subPath) {
        File folder = resolvePath(subPath).toFile();
        System.out.println("접근 경로: " + folder.getAbsolutePath());

        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("디렉토리가 존재하지 않거나 디렉토리가 아님: " + folder.getAbsolutePath());
        }

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

    // 파일 다운로드용 리소스 반환
    public Resource getFile(String filePath) {
        try {
            Path path = resolvePath(filePath);
            if (!Files.exists(path) || Files.isDirectory(path)) {
                throw new RuntimeException("파일을 찾을 수 없거나 디렉토리입니다: " + filePath);
            }
            return new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException("파일 경로 오류: " + filePath, e);
        }
    }

    // 파일 저장
    public void saveFile(MultipartFile file, String subDir) {
        try {
            Path dirPath = resolvePath(subDir);
            Files.createDirectories(dirPath); // 디렉토리가 없으면 생성
            Path target = dirPath.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    // 디렉토리 생성
    public void createDirectory(String subPath) {
        try {
            Path dir = resolvePath(subPath);
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("디렉토리 생성 실패", e);
        }
    }

    // 파일 또는 디렉토리 삭제
    public void deletePath(String subPath) {
        Path path = resolvePath(subPath);
        try {
            if (Files.notExists(path)) return;

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
