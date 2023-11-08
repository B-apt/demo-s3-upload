package com.example.demo.dto;

import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds upload state during a multipart upload
 */
public class S3UploadState {

    public String bucket;

    public String filekey;

    public String uploadId;

    public int partCounter;

    public Map<Integer, CompletedPart> completedParts = new HashMap<>();

    public S3UploadState(final String bucket, final String filekey) {
        this.bucket = bucket;
        this.filekey = filekey;
    }
}
