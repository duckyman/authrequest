package com.poelov.authrequest.config;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.ICredentialProvider;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.ecs20140526.AsyncClient;
import com.aliyun.sdk.service.ecs20140526.models.DescribeInstanceStatusRequest;
import com.aliyun.sdk.service.ecs20140526.models.StartInstanceRequest;
import com.aliyun.sdk.service.ecs20140526.models.StopInstanceRequest;
import darabonba.core.client.ClientOverrideConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "config")
public class Config {
    private String accessKeyId;
    private String accessKeySecret;
    private String regionId;
    private String endpoint;
    private String instanceId;
    private String stoppedMode;
    private Long waitDuration;

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getStoppedMode() {
        return stoppedMode;
    }

    public void setStoppedMode(String stoppedMode) {
        this.stoppedMode = stoppedMode;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public Long getWaitDuration() {
        return waitDuration;
    }

    public void setWaitDuration(Long waitDuration) {
        this.waitDuration = waitDuration;
    }

    @Bean
    public ICredentialProvider provider() {
        return StaticCredentialProvider.create(Credential.builder()
                .accessKeyId(getAccessKeyId())
                .accessKeySecret(getAccessKeySecret())
                .build());
    }

    @Bean
    public AsyncClient asyncClient() {
        return AsyncClient.builder()
                .region(getRegionId())
                .credentialsProvider(provider())
                .overrideConfiguration(ClientOverrideConfiguration.create().setEndpointOverride(getEndpoint()))
                .build();
    }

    @Bean
    public StartInstanceRequest startInstanceRequest() {
        return StartInstanceRequest.builder()
                .instanceId(getInstanceId())
                .build();
    }

    @Bean
    public StopInstanceRequest stopInstanceRequest() {
        return StopInstanceRequest.builder()
                .instanceId(getInstanceId())
                .stoppedMode(getStoppedMode())
                .build();
    }

    @Bean
    public DescribeInstanceStatusRequest describeInstanceStatusRequest() {
        return DescribeInstanceStatusRequest.builder()
                .regionId(getRegionId())
                .instanceId(List.of(getInstanceId()))
                .build();
    }
}
