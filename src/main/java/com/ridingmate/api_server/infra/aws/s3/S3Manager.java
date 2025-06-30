package com.ridingmate.api_server.infra.aws.s3;

import com.ridingmate.api_server.infra.aws.AwsProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Manager {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AwsProperty awsProperty;

    //TODO 예외처리 로직 생성필요
    public void uploadFile(String key, MultipartFile file) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(awsProperty.s3().bucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            log.info("S3 업로드 완료: {}", key);
        } catch (Exception e) {
            log.error("S3 업로드 실패: {}", key, e);
            throw new RuntimeException("S3 업로드 실패", e);
        }
    }

    public String getPresignedUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(awsProperty.s3().bucket())
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofMinutes(30))
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    public InputStream downloadFile(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(awsProperty.s3().bucket())
                .key(key)
                .build();

        return s3Client.getObject(getObjectRequest);
    }

    public void deleteFile(String key) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(awsProperty.s3().bucket())
                .key(key)
                .build();

        s3Client.deleteObject(deleteRequest);
        log.info("S3 삭제 완료: {}", key);
    }
}