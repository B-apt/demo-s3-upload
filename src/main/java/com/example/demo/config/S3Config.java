package com.example.demo.config;

import static com.example.demo.config.ConcurrencyStaticConfig.S3_SDK_MAX_CONCURRENCY;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class S3Config {

    private final Environment env;

    @Bean
    public S3AsyncClient s3AsyncClient(final AwsCredentialsProvider credentialsProvider)
            throws URISyntaxException {
        SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()
                .writeTimeout(Duration.ZERO)
                .maxConcurrency(S3_SDK_MAX_CONCURRENCY)
                .build();

        S3Configuration serviceConfiguration = S3Configuration.builder()
                .checksumValidationEnabled(Boolean.FALSE)
                .chunkedEncodingEnabled(Boolean.TRUE)
                .build();

        return S3AsyncClient.builder()
                .httpClient(httpClient)
                .endpointOverride(new URI(this.env.getProperty("S3_ENDPOINT")))
                .region(Region.of(this.env.getProperty("S3_REGION")))
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(serviceConfiguration)
                .forcePathStyle(Boolean.TRUE)
                .build();
    }

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        return () -> AwsBasicCredentials.create(this.env.getProperty("S3_ACCESSKEYID"), this.env.getProperty("S3_SECRETKEY"));
    }
}
