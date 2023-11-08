package com.example.demo.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class S3MetadataConstant {

    public static final String PREFIX = "x-amz-meta-";

    public static final String FILE_NAME = "filename";

    public static String concatPrefixTo(String metadataKey) {
        return PREFIX + metadataKey;
    }
}
