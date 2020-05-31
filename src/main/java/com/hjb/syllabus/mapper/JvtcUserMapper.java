package com.hjb.syllabus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjb.syllabus.entity.dto.UserRobotDTO;
import com.hjb.syllabus.entity.po.JvtcUser;

import java.util.List;

/**
 * jvtc mapper
 *
 * @author h1525
 */
public interface JvtcUserMapper extends BaseMapper<JvtcUser> {

    /**
     * 获取用户机器人信息
     *
     * @return
     */
    List<UserRobotDTO> selectUserRobotList();

    /**
     * 根据机器人目标号码对应的用户
     *
     * @param target
     * @return
     */
    List<JvtcUser> selectUserByRobotTarget(String target);
}