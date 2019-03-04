/*
 * 项目名称:platform-plus
 * 类名称:SysConfigServiceImpl.java
 * 包名称:com.platform.modules.sys.service.impl
 *
 * 修改履历:
 *      日期                修正者      主要内容
 *      2018/11/21 16:04    李鹏军      初版完成
 *
 * Copyright (c) 2019-2019 微同软件
 */
package com.platform.modules.sys.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.platform.common.exception.BusinessException;
import com.platform.common.utils.Constant;
import com.platform.common.utils.JedisUtil;
import com.platform.common.utils.PageUtils;
import com.platform.common.utils.Query;
import com.platform.modules.sys.dao.SysConfigDao;
import com.platform.modules.sys.entity.SysConfigEntity;
import com.platform.modules.sys.service.SysConfigService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Map;

/**
 * @author 李鹏军
 */
@Service("sysConfigService")
public class SysConfigServiceImpl extends ServiceImpl<SysConfigDao, SysConfigEntity> implements SysConfigService {
    @Autowired
    private JedisUtil jedisUtils;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String paramKey = (String) params.get("paramKey");

        Page<SysConfigEntity> page = this.selectPage(
                new Query<SysConfigEntity>(params).getPage(),
                new EntityWrapper<SysConfigEntity>()
                        .like(StringUtils.isNotBlank(paramKey), "param_key", paramKey)
                        .eq("status", 1)
        );

        return new PageUtils(page);
    }

    @Override
    public void save(SysConfigEntity config) {
        this.insert(config);
        saveOrUpdateFromRedis(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysConfigEntity config) {
        this.updateAllColumnById(config);
        saveOrUpdateFromRedis(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateValueByKey(String key, String value) {
        baseMapper.updateValueByKey(key, value);
        deleteFromRedis(key);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(String[] ids) {
        for (String id : ids) {
            SysConfigEntity config = this.selectById(id);
            deleteFromRedis(config.getParamKey());
        }

        this.deleteBatchIds(Arrays.asList(ids));
    }

    @Override
    public String getValue(String key) {
        SysConfigEntity config = getFromRedis(key);
        if (config == null) {
            config = baseMapper.queryByKey(key);
            saveOrUpdateFromRedis(config);
        }

        return config == null ? null : config.getParamValue();
    }

    @Override
    public <T> T getConfigObject(String key, Class<T> clazz) {
        String value = getValue(key);
        if (StringUtils.isNotBlank(value)) {
            return new Gson().fromJson(value, clazz);
        }

        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new BusinessException("获取参数失败");
        }
    }

    private void saveOrUpdateFromRedis(SysConfigEntity config) {
        if (config == null) {
            return;
        }
        String key = Constant.SYS_CACHE + config.getParamKey();
        jedisUtils.setObject(key, config);
    }

    private void deleteFromRedis(String configKey) {
        String key = Constant.SYS_CACHE + configKey;
        jedisUtils.del(key);
    }

    private SysConfigEntity getFromRedis(String configKey) {
        String key = Constant.SYS_CACHE + configKey;
        return (SysConfigEntity) jedisUtils.getObject(key);
    }
}
