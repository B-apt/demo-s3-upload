package com.example.demo.config;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ConcurrencyStaticConfig {

    public static final int FLATMAP_UPLOAD_CONCURRENCY = 10;

    public static final int FILECONTEXT_READ_BUFFER_SIZE = 5 * 1024 * 1024;

    public static final int S3_SDK_MAX_CONCURRENCY = 100;
}
