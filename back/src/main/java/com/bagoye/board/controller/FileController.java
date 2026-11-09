package com.bagoye.board.controller;

import com.bagoye.board.common.ImageFileType;
import com.bagoye.board.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public String upload(
            @RequestParam("file") MultipartFile file
    ) {
        String url = fileService.upload(file);
        return url;
    }

    @GetMapping("{fileName}")
    public ResponseEntity<Resource> getImage(
            @PathVariable("fileName") String fileName
    ) {
        Resource resource = fileService.getImage(fileName);
        if (resource == null) return ResponseEntity.notFound().build();

        ImageFileType imageFileType = ImageFileType.fromFileName(fileName);
        return ResponseEntity.ok()
                .contentType(imageFileType.getMediaType())
                .body(resource);
    }
}
