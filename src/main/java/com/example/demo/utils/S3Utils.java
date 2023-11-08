package com.example.demo.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Map;

import com.example.demo.constant.S3MetadataConstant;
import com.example.demo.dto.FileContext;

import lombok.experimental.UtilityClass;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.model.S3Response;

@UtilityClass
public class S3Utils {

    private static final String S3_PREFIX = "direct-memory-panic/";

    public static String getS3Key() {
        return S3_PREFIX + "0b9c1542-ab8f-4d0c-8fe7-ec365b118db7";
    }

    public static Map<String, String> buildUploadMetadata(final FileContext s3FileContext) {
        return Map.ofEntries(
            new AbstractMap.SimpleEntry<>(
                S3MetadataConstant.concatPrefixTo(S3MetadataConstant.FILE_NAME),
                encodeToUtf8(s3FileContext.getFilename())));
    }

    public static String encodeToUtf8(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    public static Mono<Void> checkS3Response(final S3Response s3Response) {
        if (s3Response.sdkHttpResponse() == null || !s3Response.sdkHttpResponse().isSuccessful()) {
            return Mono.error(new ArrayStoreException("Error upload s3"));
        }
        return Mono.empty();
    }
}
