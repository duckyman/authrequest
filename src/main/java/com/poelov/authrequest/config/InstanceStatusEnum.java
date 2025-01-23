package com.poelov.authrequest.config;

public enum InstanceStatusEnum {
    /**
     * 创建中
     */
    Pending,

    /**
     * 运行中
     */
    Running,

    /**
     * 启动中
     */
    Starting,

    /**
     * 停止中
     */
    Stopping,

    /**
     * 已停止
     */
    Stopped,
}
