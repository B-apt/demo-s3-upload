package com.example.demo.service;

import static com.example.demo.config.ConcurrencyStaticConfig.FLATMAP_UPLOAD_CONCURRENCY;
import static com.example.demo.utils.S3Utils.buildUploadMetadata;
import static com.example.demo.utils.S3Utils.checkS3Response;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.springframework.core.env.Environment;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;

import com.example.demo.dto.FileContext;
import com.example.demo.dto.S3UploadState;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {

    private final S3AsyncClient s3AsyncClient;

    private final Environment env;

    public Mono<Void> uploadMultipart(final FileContext fileContext) {
        return Mono.defer(() -> {
            LOGGER.info("Upload multipart file: bucketName={}, filekey={}",
                this.getBucket(), fileContext.getS3Key());

            final S3UploadState uploadState =
                new S3UploadState(this.getBucket(), fileContext.getS3Key());

            return this.createMultipartUpload(fileContext)
                .flatMapMany(
                    response -> this.startReadFileContent(fileContext, uploadState, response))
                .flatMap(dataBuffer -> this.uploadPart(uploadState, dataBuffer),
                    FLATMAP_UPLOAD_CONCURRENCY)
                .reduce(uploadState, UploadService::reduceUploadingPart)
                .flatMap(this::completeUpload)
                .then();
        });
    }

    private Flux<DataBuffer> startReadFileContent(final FileContext fileContext,
        final S3UploadState uploadState, final CreateMultipartUploadResponse response) {
        uploadState.uploadId = response.uploadId();
        LOGGER.info("Starting read of file, uploadId={}", response.uploadId());
        return fileContext.getContent();
    }

    private Mono<CreateMultipartUploadResponse> createMultipartUpload(
        final FileContext fileContext) {
        CompletableFuture<CreateMultipartUploadResponse> createMultipartUploadRequest =
            s3AsyncClient
                .createMultipartUpload(CreateMultipartUploadRequest.builder()
                    .contentType(fileContext.getMediaType().toString())
                    .metadata(buildUploadMetadata(fileContext))
                    .key(fileContext.getS3Key())
                    .bucket(this.getBucket())
                    .build());

        return Mono.fromFuture(createMultipartUploadRequest)
            .flatMap(createMultipartUploadResponse ->
                checkS3Response(createMultipartUploadResponse)
                    .thenReturn(createMultipartUploadResponse));
    }

    private Mono<CompletedPart> uploadPart(final S3UploadState uploadState,
        final DataBuffer dataBuffer) {
        final int partNumber = ++uploadState.partCounter;
        LOGGER.info("Upload part: partNumber={}, contentLength={}", partNumber,
            dataBuffer.readableByteCount());

        final CompletableFuture<UploadPartResponse> uploadPartRequestFuture =
            this.s3AsyncClient
                .uploadPart(
                    UploadPartRequest.builder()
                        .bucket(uploadState.bucket)
                        .key(uploadState.filekey)
                        .partNumber(partNumber)
                        .uploadId(uploadState.uploadId)
                        .contentLength((long) dataBuffer.readableByteCount())
                        .build(),
                    AsyncRequestBody
                        .fromPublisher(dataBufferToByteBufferFluxWithClose(dataBuffer)));

        return Mono.fromFuture(uploadPartRequestFuture)
            .doFinally(signalType -> releaseDataBuffer(dataBuffer, signalType))
            .flatMap(uploadPartResult -> {
                LOGGER.info("Upload part complete: part={}, etag={}", partNumber,
                    uploadPartResult.eTag());

                return checkS3Response(uploadPartResult)
                    .thenReturn(CompletedPart.builder()
                        .eTag(uploadPartResult.eTag())
                        .partNumber(partNumber)
                        .build());
            });
    }

    private static void releaseDataBuffer(final DataBuffer dataBuffer,
        final SignalType signalType) {
//        if (signalType.equals(CANCEL) || signalType.equals(ON_ERROR)) {
//            Mono.delay(Duration.ofSeconds(5))
//                .doOnNext(unused -> {
//                    LOGGER.info("releaseDataBuffer() waited 5sec for release of the dataBuffer");
//                    DataBufferUtils.release(dataBuffer);
//                }).subscribe();
//        } else {
            LOGGER.info("releaseDataBuffer() releasing NOW dataBuffer");
            DataBufferUtils.release(dataBuffer);
//        }
    }

    private static Flux<ByteBuffer> dataBufferToByteBufferFluxWithClose(DataBuffer dataBuffer) {
        final DataBuffer.ByteBufferIterator byteBufferIterator = dataBuffer.readableByteBuffers();
        return Flux.fromIterable(() -> byteBufferIterator)
            .doFinally(signalType -> byteBufferIterator.close());
    }

    private Mono<CompleteMultipartUploadResponse> completeUpload(final S3UploadState uploadState) {
        LOGGER.info("CompleteUpload: bucket={}, filekey={}, completedParts.size={}",
            uploadState.bucket, uploadState.filekey, uploadState.completedParts.size());

        return Mono.fromFuture(s3AsyncClient
            .completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                .bucket(uploadState.bucket)
                .uploadId(uploadState.uploadId)
                .multipartUpload(multipartUpload -> multipartUpload
                    .parts(uploadState.completedParts.values()))
                .key(uploadState.filekey)
                .build()));
    }

    private static S3UploadState reduceUploadingPart(final S3UploadState uploadState,
        final CompletedPart completedPart) {
        uploadState.completedParts.put(completedPart.partNumber(), completedPart);
        return uploadState;
    }

    private String getBucket() {
        return this.env.getProperty("S3_BUCKET");
    }
}
