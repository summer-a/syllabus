package com.hjb.syllabus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjb.syllabus.entity.dto.UserRobotDTO;
import com.hjb.syllabus.entity.po.JvtcUser;
import com.hjb.syllabus.entity.po.Robot;

import java.util.List;

/**
 * jvtc mapper
 *
 * @author h1525
 */
public interface RobotMapper extends BaseMapper<Robot> {

    /**
     * 分页获取
     * @param page
     * @param state
     * @return
     */
    IPage<Robot> selectPageVo(Page<Robot> page, Integer state);
}