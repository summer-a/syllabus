package com.hjb.syllabus.service;

import com.hjb.syllabus.entity.po.JvtcUser;
import com.hjb.syllabus.entity.vo.ResponseVO;
import us.codecraft.webmagic.selector.Html;

/**
 * @author 胡江斌
 * @version 1.0
 * @title: SyllabusService
 * @projectName syllabus
 * @description: TODO
 * @date 2020/5/28 12:22
 */
public interface SyllabusService {

    /**
     * 获取课程表
     *
     * @param howWeeks 当前第几周(绝对周)
     * @param jvtcUser 用户
     * @param retryCount 重试次数
     * @return
     */
    Html getTimeTable(Integer howWeeks, JvtcUser jvtcUser, int retryCount);

    /**
     * 进行登录
     *
     * @param username 用户名
     * @param encoded  压缩的密码
     * @return 如果返回401表示账号密码有误
     */
    ResponseVO loginByUserNameAndEncode(String username, String encoded);

    /**
     * 当前周次
     * @return
     */
    int nowWeek();

    /**
     * 获取登录状态
     * @param html
     * @return
     */
    boolean isLogined(Html html);

    /**
     * 获取登录状态
     * @param cookie
     * @return
     */
    boolean isLogined(String cookie);
}
