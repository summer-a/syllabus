package com.hjb.syllabus.service.baseimpl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjb.syllabus.entity.base.BaseEntity;
import com.xiaoleilu.hutool.util.CollectionUtil;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * @author 胡江斌
 * @version 1.0
 * @title: BaseServiceImpl
 * @projectName syllabus
 * @description: TODO
 * @date 2020/5/30 23:03
 */
public class BaseServiceImpl<M extends BaseMapper<T>, T extends BaseEntity> extends ServiceImpl<M, T> {

    /**
     * 批量插入
     *
     * @param entityList ignore
     * @param batchSize  ignore
     * @return ignore
     */
    @Override
    public boolean saveBatch(Collection<T> entityList, int batchSize) {
        this.recordTimes(entityList);
        return super.saveBatch(entityList, batchSize);
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<T> entityList, int batchSize) {
        this.recordTimes(entityList);
        return super.saveOrUpdateBatch(entityList, batchSize);
    }

    @Override
    public boolean updateBatchById(Collection<T> entityList, int batchSize) {
        this.recordUpdateTimes(entityList);
        return super.updateBatchById(entityList, batchSize);
    }

    /**
     * 插入一条记录（选择字段，策略插入）
     *
     * @param entity 实体对象
     */
    @Override
    public boolean save(T entity) {
        this.recordTime(entity);
        return super.save(entity);
    }

    /**
     * 插入（批量）
     *
     * @param entityList 实体对象集合
     */
    @Override
    public boolean saveBatch(Collection<T> entityList) {
        this.recordTimes(entityList);
        return super.saveBatch(entityList);
    }

    /**
     * 批量修改插入
     *
     * @param entityList 实体对象集合
     */
    @Override
    public boolean saveOrUpdateBatch(Collection<T> entityList) {
        this.recordTimes(entityList);
        return super.saveOrUpdateBatch(entityList);
    }

    /**
     * 根据 ID 选择修改
     *
     * @param entity 实体对象
     */
    @Override
    public boolean updateById(T entity) {
        this.recordUpdateTime(entity);
        return super.updateById(entity);
    }

    /**
     * 根据 UpdateWrapper 条件，更新记录 需要设置sqlset
     *
     * @param updateWrapper 实体对象封装操作类 {@link UpdateWrapper}
     */
    @Override
    public boolean update(Wrapper<T> updateWrapper) {
        T entity = updateWrapper.getEntity();
        this.recordUpdateTime(entity);
        return super.update(updateWrapper);
    }

    /**
     * 根据 whereEntity 条件，更新记录
     *
     * @param entity        实体对象
     * @param updateWrapper 实体对象封装操作类 {@link UpdateWrapper}
     */
    @Override
    public boolean update(T entity, Wrapper<T> updateWrapper) {
        this.recordUpdateTime(entity);
        return super.update(entity, updateWrapper);
    }

    /**
     * 根据ID 批量更新
     *
     * @param entityList 实体对象集合
     */
    @Override
    public boolean updateBatchById(Collection<T> entityList) {
        this.recordUpdateTimes(entityList);
        return super.updateBatchById(entityList);
    }

    /**
     * 批量记录插入和更新时间
     *
     * @param entityList
     */
    private void recordTimes(Collection<T> entityList) {
        if (CollectionUtil.isNotEmpty(entityList)) {
            entityList.forEach(list -> {
                list.setCreateTime(LocalDateTime.now());
                list.setUpdateTime(LocalDateTime.now());
            });
        }
    }

    /**
     * 记录更新时间
     *
     * @param entityList
     */
    private void recordUpdateTimes(Collection<T> entityList) {
        if (CollectionUtil.isNotEmpty(entityList)) {
            entityList.forEach(list -> list.setUpdateTime(LocalDateTime.now()));
        }
    }

    /**
     * 批量记录更新时间
     *
     * @param entity
     */
    private void recordUpdateTime(T entity) {
        if (null != entity) {
            entity.setUpdateTime(LocalDateTime.now());
        }
    }

    /**
     * 记录插入和更新时间
     *
     * @param entity
     */
    private void recordTime(T entity) {
        if (null != entity) {
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
        }
    }

}
