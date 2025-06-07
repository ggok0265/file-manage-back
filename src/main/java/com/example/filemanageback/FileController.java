package com.example.filemanageback;

import com.example.filemanageback.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/files")
    public List<Map<String, String>> listFiles(@RequestParam(defaultValue = "/") String path) {
        return fileService.listFiles(path);
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String path) {
        Resource resource = fileService.getFile(path);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam(defaultValue = "") String path) {
        fileService.saveFile(file, path);
        return ResponseEntity.ok("업로드 완료");
    }

    @PostMapping("/directory")
    public ResponseEntity<String> createDirectory(@RequestBody Map<String, String> req) {
        String path = req.get("path");
        fileService.createDirectory(path);
        return ResponseEntity.ok("디렉토리 생성 완료");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deletePath(@RequestParam String path) {
        fileService.deletePath(path);
        return ResponseEntity.ok("삭제 완료");
    }
}
