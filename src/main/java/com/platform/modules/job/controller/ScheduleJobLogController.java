/*
 * 项目名称:platform-plus
 * 类名称:ScheduleJobLogController.java
 * 包名称:com.platform.modules.job.controller
 *
 * 修改履历:
 *      日期                修正者      主要内容
 *      2018/11/21 16:04    李鹏军      初版完成
 *
 * Copyright (c) 2019-2019 微同软件
 */
package com.platform.modules.job.controller;

import com.platform.common.utils.PageUtils;
import com.platform.common.utils.RestResponse;
import com.platform.modules.job.entity.ScheduleJobLogEntity;
import com.platform.modules.job.service.ScheduleJobLogService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 定时任务日志
 *
 * @author 李鹏军
 */
@RestController
@RequestMapping("/sys/scheduleLog")
public class ScheduleJobLogController {
    @Autowired
    private ScheduleJobLogService scheduleJobLogService;

    /**
     * 定时任务日志列表
     */
    @GetMapping("/list")
    @RequiresPermissions("sys:schedule:log")
    public RestResponse list(@RequestParam Map<String, Object> params) {
        PageUtils page = scheduleJobLogService.queryPage(params);

        return RestResponse.success().put("page", page);
    }

    /**
     * 定时任务日志信息
     */
    @GetMapping("/info/{logId}")
    public RestResponse info(@PathVariable("logId") String logId) {
        ScheduleJobLogEntity log = scheduleJobLogService.selectById(logId);

        return RestResponse.success().put("log", log);
    }
}
