package com.ridingmate.api_server.infra.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

@Configuration
public class AwsConfig {

    private final AwsProperty awsProperty;

    public AwsConfig(AwsProperty awsProperty) {
        this.awsProperty = awsProperty;
    }

    @Bean
    public StaticCredentialsProvider awsCredentialsProvider() {
        AwsBasicCredentials creds = AwsBasicCredentials.create(
                awsProperty.credentials().accessKey(),
                awsProperty.credentials().secretKey()
        );
        return StaticCredentialsProvider.create(creds);
    }
}