package com.ridingmate.api_server.infra.aws.sqs;

import com.ridingmate.api_server.infra.aws.AwsProperty;
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class SqsConfig {

    private final AwsProperty awsProperty;

    public SqsConfig(AwsProperty awsProperty) {
        this.awsProperty = awsProperty;
    }

    @Bean
    public SqsAsyncClient sqsClient(StaticCredentialsProvider awsCredentialsProvider) {
        return SqsAsyncClient.builder()
            .region(awsProperty.regionAsEnum())
            .credentialsProvider(awsCredentialsProvider)
            .build();
    }

    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(
        SqsAsyncClient sqsAsyncClient
    ) {
        return SqsMessageListenerContainerFactory
            .builder()
            .sqsAsyncClient(sqsAsyncClient)
            .build();
    }
}