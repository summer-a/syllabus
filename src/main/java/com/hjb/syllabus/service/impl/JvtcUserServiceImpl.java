package com.hjb.syllabus.service.impl;

import com.hjb.syllabus.entity.dto.UserRobotDTO;
import com.hjb.syllabus.entity.po.JvtcUser;
import com.hjb.syllabus.mapper.JvtcUserMapper;
import com.hjb.syllabus.service.JvtcUserService;
import com.hjb.syllabus.service.baseimpl.BaseServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 胡江斌
 * @version 1.0
 * @title: JvtcUserServiceImpl
 * @projectName blog
 * @description: TODO
 * @date 2019/6/27 23:28
 */
@Service
public class JvtcUserServiceImpl extends BaseServiceImpl<JvtcUserMapper, JvtcUser> implements JvtcUserService {

    @Resource
    private JvtcUserMapper jvtcUserMapper;

    /**
     * 获取所有有效的用户对应机器人信息
     *
     * @return
     */
    @Transactional(readOnly = true, rollbackFor = RuntimeException.class)
    @Override
    public List<UserRobotDTO> selectUserRobotList() {
        return jvtcUserMapper.selectUserRobotList();
    }
}
