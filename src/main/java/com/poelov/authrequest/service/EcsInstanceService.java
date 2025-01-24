package com.poelov.authrequest.service;

import com.aliyun.sdk.service.ecs20140526.models.DescribeInstanceStatusResponseBody;
import com.poelov.authrequest.config.InstanceStatusEnum;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public interface EcsInstanceService {
    /**
     * 启动实例
     */
    void startInstance();

    /**
     * 同步实例状态
     */
    void syncInstanceStatus() throws ExecutionException, InterruptedException;

    /**
     * 查询实例状态
     */
    void queryInstanceStatus(Consumer<DescribeInstanceStatusResponseBody.InstanceStatus> consumer) throws ExecutionException, InterruptedException;

    /**
     * 获取实例状态 (内部维护的状态)
     */
    InstanceStatusEnum getInstanceStatus(String instanceId);

    /**
     * 停止实例
     */
    void stopInstance() throws ExecutionException, InterruptedException;
}
