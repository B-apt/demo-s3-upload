package com.example.demo.dto;

import static com.example.demo.config.ConcurrencyStaticConfig.FILECONTEXT_READ_BUFFER_SIZE;

import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.MediaType;

import io.netty.buffer.ByteBufAllocator;
import lombok.Builder;
import lombok.Data;
import reactor.core.publisher.Flux;

@Data
@Builder
public class FileContext {

    private final String s3Key;

    private final MediaType mediaType;

    private Path filePath;

    private String filename;

    public Flux<DataBuffer> getContent() {
        return DataBufferUtils
            .readAsynchronousFileChannel(
                () -> AsynchronousFileChannel.open(filePath, StandardOpenOption.READ),
                new NettyDataBufferFactory(ByteBufAllocator.DEFAULT), FILECONTEXT_READ_BUFFER_SIZE);
    }

}
