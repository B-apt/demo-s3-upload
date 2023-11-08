package com.example.demo.controller;

import java.nio.file.Path;

import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.FileContext;
import com.example.demo.service.UploadService;
import com.example.demo.utils.S3Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Slf4j
public class UploadController {

    private final UploadService uploadService;

    private final Environment env;

    @PostMapping(value = "/upload", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<Void> upload() {
        FileContext fileContext = FileContext.builder()
            .filePath(this.getFilePath())
            .filename("video.mp4")
            .mediaType(MediaType.APPLICATION_OCTET_STREAM)
            .s3Key(S3Utils.getS3Key()).build();

        LOGGER.info("upload() starting for context:{}", fileContext);
        return this.uploadService.uploadMultipart(fileContext);
    }

    private Path getFilePath() {
        return Path.of(this.env.getProperty("FILE_PATH_UPLOAD"));
    }
}
