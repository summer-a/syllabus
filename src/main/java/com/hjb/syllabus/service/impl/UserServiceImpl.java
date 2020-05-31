package com.hjb.syllabus.service.impl;

import com.hjb.syllabus.entity.po.JvtcUser;
import com.hjb.syllabus.entity.po.User;
import com.hjb.syllabus.mapper.JvtcUserMapper;
import com.hjb.syllabus.mapper.UserMapper;
import com.hjb.syllabus.service.UserService;
import com.hjb.syllabus.service.baseimpl.BaseServiceImpl;
import com.xiaoleilu.hutool.util.CollectionUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 胡江斌
 * @version 1.0
 * @title: UserServiceImpl
 * @projectName blog
 * @description: TODO
 * @date 2019/6/9 10:09
 */
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private JvtcUserMapper jvtcUserMapper;

    /**
     * 根据机器人目标号码找对应的用户
     *
     * @param target
     * @return
     */
    @Transactional(readOnly = true, rollbackFor = RuntimeException.class)
    @Override
    public JvtcUser selectUserByRobotTarget(String target) {
        List<JvtcUser> jvtcUsers = jvtcUserMapper.selectUserByRobotTarget(target);
        if (CollectionUtil.isNotEmpty(jvtcUsers)) {
            return jvtcUsers.get(0);
        }
        return null;
    }
}
