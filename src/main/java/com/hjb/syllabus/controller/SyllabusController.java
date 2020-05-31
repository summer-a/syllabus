package com.hjb.syllabus.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjb.syllabus.entity.fields.HTMLFields;
import com.hjb.syllabus.entity.fields.RedisFields;
import com.hjb.syllabus.entity.fields.SessionFields;
import com.hjb.syllabus.entity.fields.UrlFields;
import com.hjb.syllabus.entity.po.JvtcUser;
import com.hjb.syllabus.entity.po.Robot;
import com.hjb.syllabus.entity.vo.LayuiTableVO;
import com.hjb.syllabus.entity.vo.ResultVO;
import com.hjb.syllabus.service.JvtcUserService;
import com.hjb.syllabus.service.RedisService;
import com.hjb.syllabus.service.RobotService;
import com.hjb.syllabus.service.SyllabusService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import us.codecraft.webmagic.selector.Html;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 课表显示
 * @author 胡江斌
 * @version 1.0
 * @title: TimeTableController
 * @projectName blog
 * @description: TODO
 * @date 2019/6/25 20:26
 */
@Controller
@RequestMapping("/kb")
public class SyllabusController {

    @Resource
    private JvtcUserService jvtcUserService;

    @Resource
    private RedisService redisService;

    @Resource
    private SyllabusService syllabusService;

    /**
     * 课表页面
     * @param id id
     * @param week 周次
     * @param refresh 是否强制刷新
     * @throws IOException
     */
    @GetMapping(value = {"/{id}", "/{id}/{week}", "/{id}/{week}/{refresh}"})
    public String page(Model model,
                       @PathVariable String id,
                       @PathVariable(required = false) Integer week,
                       @PathVariable(required = false) Boolean refresh) {

        // 默认本周
        week = week == null ? syllabusService.nowWeek() : week;

        JvtcUser userParam = new JvtcUser();
        userParam.setUsername(id);
        Wrapper<JvtcUser> wrapper = new QueryWrapper<>(userParam);
        JvtcUser jvtcUser = jvtcUserService.getOne(wrapper);
        if (jvtcUser == null) {
            model.addAttribute(SessionFields.TABLE_HTML, HTMLFields.ADD_USER_IF_NOT_EXISTS);
        } else {
            // 删除缓存
            if (refresh != null && refresh) {
                redisService.delete(String.format(RedisFields.TABLE, id, StringUtils.isEmpty(week) ? syllabusService.nowWeek() : week));
            }
            Html timeTable = syllabusService.getTimeTable(week, jvtcUser, 3);
            // 强制刷新链接
            model.addAttribute(SessionFields.TABLE_REFRESH_URL, String.format(UrlFields.TABLE_REFRESH, id, week));
            model.addAttribute(SessionFields.TABLE_HTML, timeTable.get());
            model.addAttribute(RedisFields.WEEK, week);
            model.addAttribute(SessionFields.JVTC_USER_ID, jvtcUser.getUsername());
        }
        return "page/timeTable";
    }
}
