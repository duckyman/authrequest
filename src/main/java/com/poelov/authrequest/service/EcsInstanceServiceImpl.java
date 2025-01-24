package com.poelov.authrequest.service;

import com.aliyun.sdk.service.ecs20140526.AsyncClient;
import com.aliyun.sdk.service.ecs20140526.models.DescribeInstanceStatusRequest;
import com.aliyun.sdk.service.ecs20140526.models.DescribeInstanceStatusResponse;
import com.aliyun.sdk.service.ecs20140526.models.DescribeInstanceStatusResponseBody;
import com.aliyun.sdk.service.ecs20140526.models.StartInstanceRequest;
import com.aliyun.sdk.service.ecs20140526.models.StopInstanceRequest;
import com.poelov.authrequest.config.InstanceStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
public class EcsInstanceServiceImpl implements EcsInstanceService {
    private final AsyncClient asyncClient;
    private final StartInstanceRequest startInstanceRequest;
    private final StopInstanceRequest stopInstanceRequest;
    private final DescribeInstanceStatusRequest describeInstanceStatusRequest;
    private static final Logger log = LoggerFactory.getLogger(EcsInstanceServiceImpl.class);
    private final ConcurrentHashMap<String, InstanceStatusEnum> instanceStatusMap = new ConcurrentHashMap<>();

    public EcsInstanceServiceImpl(AsyncClient asyncClient, StartInstanceRequest startInstanceRequest, StopInstanceRequest stopInstanceRequest, DescribeInstanceStatusRequest describeInstanceStatusRequest) {
        this.asyncClient = asyncClient;
        this.startInstanceRequest = startInstanceRequest;
        this.stopInstanceRequest = stopInstanceRequest;
        this.describeInstanceStatusRequest = describeInstanceStatusRequest;
    }

    /**
     * 启动实例
     */
    @Override
    public void startInstance() {
        String instanceId = startInstanceRequest.getInstanceId();
        InstanceStatusEnum status = getInstanceStatus(instanceId);
        // 实例状态为未知或已经停止时，才可以执行启动操作
        if (status == InstanceStatusEnum.UNKNOWN || status == InstanceStatusEnum.Stopped) {
            asyncClient.startInstance(startInstanceRequest).whenComplete((res, ex) -> {
                if (ex != null) {
                    log.error(ex.getMessage(), ex);
                    return;
                }
                if (res.getStatusCode() == HttpStatus.OK.value()) {
                    log.info("Starting Instance {} ...", startInstanceRequest.getInstanceId());
                    instanceStatusMap.put(startInstanceRequest.getInstanceId(), InstanceStatusEnum.Starting);
                }
            });
        }
    }

    /**
     * 查询实例状态
     */
    @Override
    public void syncInstanceStatus() {
        queryInstanceStatus(instanceStatus -> {
            log.info("Instance {} status is {}", instanceStatus.getInstanceId(), instanceStatus.getStatus());
            instanceStatusMap.put(instanceStatus.getInstanceId(), InstanceStatusEnum.valueOf(instanceStatus.getStatus()));
        });
    }

    /**
     * 查询实例状态
     */
    @Override
    public void queryInstanceStatus(Consumer<DescribeInstanceStatusResponseBody.InstanceStatus> consumer) {
        CompletableFuture<DescribeInstanceStatusResponse> future = asyncClient.describeInstanceStatus(describeInstanceStatusRequest);
        future.whenComplete((res, ex) -> {
            if (ex != null) {
                log.error(ex.getMessage(), ex);
            }
            if (res.getStatusCode() == HttpStatus.OK.value()) {
                res.getBody().getInstanceStatuses().getInstanceStatus().forEach(instanceStatus -> {
                    if (consumer != null) {
                        consumer.accept(instanceStatus);
                    }
                });
            }
        });
    }


    /**
     * 获取实例状态
     * 实例不存在时返回未知
     */
    @Override
    public InstanceStatusEnum getInstanceStatus(String instanceId) {
        InstanceStatusEnum status = instanceStatusMap.get(instanceId);
        return Objects.requireNonNullElse(status, InstanceStatusEnum.UNKNOWN);
    }

    /**
     * 停止实例
     */
    @Override
    public void stopInstance() {
        String instanceId = startInstanceRequest.getInstanceId();
        InstanceStatusEnum status = getInstanceStatus(instanceId);
        // 实例不等于未知或已经停止或正在停止时，才可以执行停止操作
        if (status != InstanceStatusEnum.UNKNOWN && status != InstanceStatusEnum.Stopped && status != InstanceStatusEnum.Stopping) {
            asyncClient.stopInstance(stopInstanceRequest).whenComplete((res, ex) -> {
                if (ex != null) {
                    log.error(ex.getMessage(), ex);
                    return;
                }
                if (res.getStatusCode() == HttpStatus.OK.value()) {
                    log.info("Stopping Instance {} ... ", instanceId);
                    instanceStatusMap.put(instanceId, InstanceStatusEnum.Stopping);
                }
            });
        }
    }
}
