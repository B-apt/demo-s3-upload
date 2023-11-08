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

Use postman and hit one of the endpoint. See in ```UploadController.java```
