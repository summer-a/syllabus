package com.hjb.syllabus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjb.syllabus.entity.po.JvtcUser;
import com.hjb.syllabus.entity.po.User;

/**
 * 用户服务
 * @author 胡江斌
 * @version 1.0
 * @title: UserService
 * @projectName blog
 * @description: TODO
 * @date 2019/6/9 10:07
 */
public interface UserService extends IService<User> {

    /**
     * 根据机器人目标号码找对应的用户
     *
     * @param target
     * @return
     */
    JvtcUser selectUserByRobotTarget(String target);
}
