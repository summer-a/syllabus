package com.hjb.syllabus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjb.syllabus.entity.dto.UserRobotDTO;
import com.hjb.syllabus.entity.po.JvtcUser;

import java.util.List;

/**
 * @author 胡江斌
 * @version 1.0
 * @title: JvtcUserService
 * @projectName blog
 * @description: TODO
 * @date 2019/6/27 23:27
 */
public interface JvtcUserService extends IService<JvtcUser> {

    /**
     * 获取所有有效的用户对应机器人信息
     * @return
     */
    List<UserRobotDTO> selectUserRobotList();
}
