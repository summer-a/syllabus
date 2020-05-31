package com.hjb.syllabus.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjb.syllabus.entity.fields.SessionFields;
import com.hjb.syllabus.entity.po.JvtcUser;
import com.hjb.syllabus.entity.po.Robot;
import com.hjb.syllabus.entity.vo.LayuiTableVO;
import com.hjb.syllabus.entity.vo.ResultVO;
import com.hjb.syllabus.service.RobotService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 机器人报课控制器
 * @author 胡江斌
 * @version 1.0
 * @title: TimeTableController
 * @projectName blog
 * @description: TODO
 * @date 2019/6/25 20:26
 */
@Controller
@RequestMapping("/management")
public class ManagementController {

    @Resource
    private RobotService robotService;

    /**
     * 管理页主页
     * @return
     */
    @GetMapping(value = {"/index"})
    public String timeTableManager() {
        return "page/timeTableManager";
    }

    /**
     * 根据id获取机器人报课表
     * @param id id
     * @return
     */
    @GetMapping(value = "/addOrEditPage")
    public String addOrEditPage(@RequestParam(value = "id", required = false) Integer id,
                                Model model) {
        if (id != null) {
            Robot robot = robotService.getById(id);
            model.addAttribute(SessionFields.JVTC_ROBOT_INFO, robot);
        }

        return "page/addOrEditRobot";
    }

    /**
     * 获取机器人提醒列表
     * @param pageNum 开始页
     * @param pageSize 每页数量
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/getRobotMindList")
    public LayuiTableVO<Robot> getRobotMindList(
            @RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            HttpSession session) {
        JvtcUser user = (JvtcUser) session.getAttribute(SessionFields.JVTC_USER);
        if (user == null) {
            return LayuiTableVO.notSignedIn();
        }
        // 根据id分页排序
        IPage<Robot> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Robot> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1).eq("jvtc_user_id", user.getId());
        IPage<Robot> list = robotService.page(page, queryWrapper);

        LayuiTableVO<Robot> layuiTableVO = new LayuiTableVO<>();
        layuiTableVO.setCode(0);
        layuiTableVO.setCount((int) list.getTotal());
        layuiTableVO.setMsg("ok");
        layuiTableVO.setData(list.getRecords());

        return layuiTableVO;
    }

    /**
     * 添加或更新报课，根据是否有id进行判断
     * @param robot 报课机器人信息
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/addOrUpdateRobot")
    public ResultVO addOrUpdateRobot(Robot robot, HttpSession session) {

        JvtcUser jvtcUser = (JvtcUser) session.getAttribute(SessionFields.JVTC_USER);
        if (jvtcUser == null) {
            return ResultVO.build(401, "账号信息已过期，请重新登录！", null);
        }

        robot.setJvtcUserId(jvtcUser.getId());

        if (robot != null && robot.getId() != null) {
            // 更新状态？
            robot.setUpdateTime(LocalDateTime.now());
            robotService.updateById(robot);
        } else {
            if (!jvtcUser.getVip()) {
                Robot t = new Robot();
                t.setJvtcUserId(jvtcUser.getId());
                Wrapper<Robot> wrapper = new QueryWrapper<>(t);
                List<Robot> list = robotService.list(wrapper);
                if (!jvtcUser.getVip() && !CollectionUtils.isEmpty(list) && list.size() >= 1) {
                    return ResultVO.fail("普通用户只能添加一个机器人");
                }
            }
            // 添加任务计划
            robot.setCreateTime(LocalDateTime.now());
            robot.setUpdateTime(LocalDateTime.now());
            robotService.save(robot);

        }
        return ResultVO.ok();
    }

    /**
     * 根据id删除
     * @param id id
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/deleteRobot")
    public ResultVO deleteRobot(Integer id) {
        // 删除操作
        robotService.removeById(id);
        // 移除消息队列(更改了实现方式)
        //...
        return ResultVO.ok();
    }

}
