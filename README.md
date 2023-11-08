# Upload cancellation failure demo

Demo project to try and reproduce deep cancellation exception leading
to access violation and kill of the JVM


### Build

```mvn clean compile```

### Run & Usage

#### 1)

Add the following variables in your environment (like in VM options of run configuration):

```
S3_ACCESSKEYID=
S3_SECRETKEY=
S3_REGION=
S3_ENDPOINT=
S3_BUCKET=
FILE_PATH_UPLOAD= // in this one, specify the path on your disk of the file you want to upload for the test
```

#### 2)

If wanted, play with the numbers in ```ConcurrencyStaticConfig.java```


#### 3)

Use postman (or equivalent) and hit the endpoint

```localhost:8080/file/upload```

(check in  ```UploadController.java``` if uri is good)

Once you see the first logs, like those:

```
2023-11-08T14:33:35.765+01:00  INFO 24668 --- [ctor-http-nio-3] c.e.demo.controller.UploadController     : upload() starting for context:FileContext(s3Key=direct-memory-panic/0b9c1542-ab8f-4d0c-8fe7-ec365b118db7, mediaType=application/octet-stream, filePath=C:\Users\Videos\GoPro9B\GH010094.MP4, filename=video.mp4)
2023-11-08T14:33:35.774+01:00  INFO 24668 --- [ctor-http-nio-3] com.example.demo.service.UploadService   : Upload multipart file: bucketName=, filekey=direct-memory-panic/0b9c1542-ab8f-4d0c-8fe7-ec365b118db7
2023-11-08T14:33:36.452+01:00  INFO 24668 --- [nc-response-1-0] com.example.demo.service.UploadService   : Starting read of file, uploadId=uucPk70bzgoZIGRKKYF44DdqbC_mKvsuSxME0TWufP150LgA6pK4i_LzAA
2023-11-08T14:33:36.476+01:00  INFO 24668 --- [      Thread-12] com.example.demo.service.UploadService   : Upload part: partNumber=1, contentLength=5242880
2023-11-08T14:33:36.498+01:00  INFO 24668 --- [      Thread-15] com.example.demo.service.UploadService   : Upload part: partNumber=2, contentLength=5242880
2023-11-08T14:33:36.504+01:00  INFO 24668 --- [      Thread-12] com.example.demo.service.UploadService   : Upload part: partNumber=3, contentLength=5242880
2023-11-08T14:33:36.509+01:00  INFO 24668 --- [      Thread-15] com.example.demo.service.UploadService   : Upload part: partNumber=4, contentLength=5242880
(...)
```

**Cancel** your request in Postman. Most often it should triggers the bug
