package com.poelov.authrequest.controller;

import com.poelov.authrequest.config.Config;
import com.poelov.authrequest.service.EcsInstanceService;
import com.poelov.authrequest.config.InstanceStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/")
public class AuthController {
    /**
     * 上次请求时间
     */
    private LocalDateTime lastRequestTime;

    private final Config config;
    private final EcsInstanceService ecsInstanceService;
    private final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

    public AuthController(Config config, EcsInstanceService ecsInstanceService) {
        this.config = config;
        this.ecsInstanceService = ecsInstanceService;
    }

    @GetMapping("/auth")
    public ResponseEntity<Void> auth() {
        final String instanceId = config.getInstanceId();
        lastRequestTime = LocalDateTime.now();
        if (ecsInstanceService.getInstanceStatus(instanceId) != InstanceStatusEnum.Running) {
            ecsInstanceService.startInstance();
        }

        log.info("Auth Request ... lastRequestTime: {}, status: {}", FORMATTER.format(lastRequestTime), ecsInstanceService.getInstanceStatus(instanceId));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 定时任务 500 毫秒执行一次
     */
    @Scheduled(fixedRate = 500)
    public void task() {
        if (lastRequestTime == null) {
            return;
        }

        final String instanceId = config.getInstanceId();
        // 设定的无操作的等待时间
        Long waitDuration = config.getWaitDuration();
        // 距离上次请求的时间
        long requestTimeDuration = Duration.between(lastRequestTime, LocalDateTime.now()).toMillis();

        // 小于等待时间，并且服务器状态不为 Running
        if (requestTimeDuration < waitDuration && ecsInstanceService.getInstanceStatus(instanceId) != InstanceStatusEnum.Running) {
            ecsInstanceService.syncInstanceStatus();
        }

        // 超过等待时间，并且服务器状态不为 Stopped
        if (requestTimeDuration > waitDuration && ecsInstanceService.getInstanceStatus(instanceId) != InstanceStatusEnum.Stopped) {
            ecsInstanceService.stopInstance();
        }
    }
}
