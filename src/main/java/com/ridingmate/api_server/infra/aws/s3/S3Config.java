package com.ridingmate.api_server.infra.aws.s3;

import com.ridingmate.api_server.infra.aws.AwsProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;

@Configuration
public class S3Config {

    private final AwsProperty awsProperty;

    public S3Config(AwsProperty awsProperty) {
        this.awsProperty = awsProperty;
    }

    @Bean
    public S3Client s3Client(StaticCredentialsProvider awsCredentialsProvider) {
        return S3Client.builder()
                .region(awsProperty.regionAsEnum())
                .credentialsProvider(awsCredentialsProvider)
                .overrideConfiguration(
                        ClientOverrideConfiguration.builder()
                                .apiCallTimeout(Duration.ofMinutes(60))
                                .apiCallAttemptTimeout(Duration.ofMinutes(30))
                                .build()
                )
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(
            StaticCredentialsProvider awsCredentialsProvider
    ) {
        return S3Presigner.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(awsProperty.regionAsEnum())
                .build();
    }
}
