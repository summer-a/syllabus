package com.hjb.syllabus.task;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hjb.syllabus.entity.MessageInfo;
import com.hjb.syllabus.entity.QQType;
import com.hjb.syllabus.entity.TimeTablePerTime;
import com.hjb.syllabus.entity.dto.UserRobotDTO;
import com.hjb.syllabus.entity.fields.RedisFields;
import com.hjb.syllabus.entity.fields.UrlFields;
import com.hjb.syllabus.entity.po.JvtcUser;
import com.hjb.syllabus.entity.po.Robot;
import com.hjb.syllabus.entity.vo.ResponseVO;
import com.hjb.syllabus.service.JvtcUserService;
import com.hjb.syllabus.service.RedisService;
import com.hjb.syllabus.service.SyllabusService;
import com.hjb.syllabus.service.impl.SyllabusServiceImpl;
import com.hjb.syllabus.utils.HttpUtil;
import com.hjb.syllabus.utils.TimeTableUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.selector.Html;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

/**
 * @author 胡江斌
 * @version 1.0
 * @title: SendMessageTask
 * @projectName syllabus
 * @description: TODO
 * @date 2020/5/27 15:00
 */
@Slf4j
@Component
@EnableScheduling
public class SendMessageTask {

    /**
     * 每节课上课时间，记录的是每天经过的时间（单位秒,0为第一大节课）
     * 记得更新作息表
     */
    private static LocalTime[] time = {
            LocalTime.of(8, 5),
            LocalTime.of(10, 20),
            LocalTime.of(14, 05),
            LocalTime.of(16, 0),
            LocalTime.of(17, 45),
            LocalTime.of(19, 30)
    };


    private static DateTimeFormatter hhmm = DateTimeFormatter.ofPattern("HH:mm");

    private String line = "-------------------------------------------\n";

    @Value("${host.tableUrl}")
    private String TABLE_URL;

    private Boolean COURSE_PUSH = true;

    @Resource
    private JvtcUserService jvtcUserService;

    @Resource
    private RedisService<Integer> redisService;

    @Resource
    private SyllabusService syllabusService;

    /**
     * 每天获取一遍课表,获取失败则每天实时生成
     */
    @Scheduled(cron = "0 0 6 ? * MON-FRI")
    public void insertToRedis() {
        log.info("更新课表");

        List<UserRobotDTO> userRobots = jvtcUserService.selectUserRobotList();
        // 更新没存入redis的课表
        for (UserRobotDTO userRobot : userRobots) {
            syllabusService.getTimeTable(syllabusService.nowWeek(), userRobot.getJvtcUser(), 3);
        }
    }

    @Scheduled(cron = "0 10 7 ? * MON-FRI")
    public void sendInTheMorning() {
        if (COURSE_PUSH != null && COURSE_PUSH) {
            log.info("发送课表(上午)");
            send(1, false);
        }
    }

    @Scheduled(cron = "0 0 13 ? * MON-FRI")
    public void sendInTheNoon() {
        if (COURSE_PUSH != null && COURSE_PUSH) {
            log.info("发送课表(中午)");
            send(2, false);
        }
    }

    @Scheduled(cron = "0 0 17 ? * MON-FRI")
    public void sendInTheAfterNoon() {
        if (COURSE_PUSH != null && COURSE_PUSH) {
            log.info("发送课表(下午)");
            send(3, true);
        }
    }

    @Scheduled(cron = "0 20 18 ? * MON-FRI")
    public void sendInTheEvening() {
        if (COURSE_PUSH != null && COURSE_PUSH) {
            log.info("发送课表(晚上)");
            send(3, false);
        }
    }

    @Scheduled(cron = "0 01 0 ? * MON")
    public void initWeek() {
        log.info("每周一0点过一分更新周次");
        Request.Builder builder = SyllabusServiceImpl.request;

        int i = 0;
        boolean flag = false;
        do {
            // 获取表中第一个用户
            JvtcUser user = new JvtcUser();
            user.setId(1);
            Wrapper<JvtcUser> wrapper = new QueryWrapper<>(user);
            JvtcUser jvtcUser = jvtcUserService.getOne(wrapper);
            ResponseVO result = HttpUtil.get(UrlFields.GET_NOW_WEEK, builder.addHeader(UrlFields.COOKIE, jvtcUser.getCookie()));

            Matcher matcher = SyllabusServiceImpl.pattern.matcher(result.getHtml());

            if (matcher.find()) {
                // success
                String week = matcher.group(1);
                redisService.set(RedisFields.WEEK, Integer.parseInt(week));
                flag = true;
                break;
            } else {
                syllabusService.loginByUserNameAndEncode(jvtcUser.getUsername(), jvtcUser.getPassword());
            }
        } while (++i < 3);

        // fail
        if (!flag) {
            log.error("获取周次失败, 进行默认处理, + 1 周");
            try {
                redisService.set(RedisFields.WEEK, redisService.get(RedisFields.WEEK) + 1);
            } catch (Exception e) {
                log.error("获取周次默认处理失败", e);
            }
        }
    }

    /**
     * 发送
     *
     * @param interval 时间区(1早, 2中, 3晚)
     */
    private void send(int interval, boolean isFifthLesson) {

        log.info("开始发送课表");
        List<UserRobotDTO> userRobots = jvtcUserService.selectUserRobotList();

        List<MessageInfo> course = new ArrayList();

        // 获取当天课程表字符串(一个星期)
        // 拿到所有有效用户的课表, 根据班级从缓存获取
        for (UserRobotDTO userRobot : userRobots) {
            JvtcUser juser = userRobot.getJvtcUser();
            //
            Html timeTable = syllabusService.getTimeTable(0, juser, 3);
            // 转成对象(一天)
            List<TimeTablePerTime> courseOfDay = htmlToBeen(timeTable, LocalDate.now().getDayOfWeek().getValue());
            // 获取课程表
            if (courseOfDay.size() > 0) {

                Optional<TimeTablePerTime> t1 = courseOfDay.stream().filter(f -> f.getNo() == interval * 2 - 1).findFirst();
                Optional<TimeTablePerTime> t2 = courseOfDay.stream().filter(f -> f.getNo() == interval * 2).findFirst();

                if (interval == 3) {
                    if (isFifthLesson && !t1.isPresent()) {
                        continue;
                    } else if (!isFifthLesson && t1.isPresent()) {
                        continue;
                    }
                }
                // 如果是第五节大课,提前报课
                String timeTableUrl = TABLE_URL + userRobot.getJvtcUser().getUsername();
                //
                if (t1.isPresent() || t2.isPresent()) {
                    String tableText = createTableText(t1, t2, timeTableUrl);
                    this.appendInfo(course, tableText, userRobot);
                }

            }
        }
        // 发送
        TimeTableUtil.send(course);
    }

    /**
     * 创建表文本
     *
     * @param o1
     * @param o2
     * @param timeTableUrl
     * @return
     */
    private String createTableText(Optional<TimeTablePerTime> o1, Optional<TimeTablePerTime> o2, String timeTableUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append(line);
        appendTextProcess(o1, sb);
        if (o1.isPresent() && o2.isPresent()) {
            sb.append(line);
        }
        appendTextProcess(o2, sb);
        sb.append(line);
        sb.append("课表:").append(timeTableUrl);
        return sb.toString();
    }

    /**
     * html字符串转对象
     *
     * @param html 字符串
     * @param week 星期几
     * @return
     */
    private List<TimeTablePerTime> htmlToBeen(Html html, int week) {
        List<TimeTablePerTime> timeTables = new ArrayList<>();
        Document doc = html.getDocument();
        // 获取单元格
        Elements table_td = doc.select("table").select("td:nth-child(" + (week + 1) + ")");
        for (int i = 0; i < table_td.size(); i++) {
            Element element_td = table_td.get(i);
            if (!StringUtils.isEmpty(element_td.html())) {
                String title = element_td.select("p").attr("title");
                TimeTablePerTime timeTablePerTime = new TimeTablePerTime();
                if (!StringUtils.isEmpty(title)) {
                    String[] info = title.split("<br/>");
                    timeTablePerTime.setNo(i + 1);
                    timeTablePerTime.setCourseName(info[2]);
                    timeTablePerTime.setUpTime("上课时间：" + time[i].format(hhmm));
                    timeTablePerTime.setAddress(info[4]);
                    timeTables.add(timeTablePerTime);
                }
            }
        }
        return timeTables;
    }

    /**
     * 文本处理
     *
     * @param optional
     * @param sb
     * @return
     */
    private void appendTextProcess(Optional<TimeTablePerTime> optional, StringBuilder sb) {
        if (optional.isPresent()) {
            TimeTablePerTime timeTablePerTime = optional.get();
            int no = timeTablePerTime.getNo();
            String courseName = timeTablePerTime.getCourseName();
            String address = timeTablePerTime.getAddress();
            String upTimeWeekStr = timeTablePerTime.getUpTime();
            String upTimeStr = time[timeTablePerTime.getNo() - 1].format(hhmm);
            appendText(sb, no, courseName, address, upTimeWeekStr, upTimeStr);
        }
    }

    /**
     * 文本补充
     *
     * @param sb
     * @param no
     * @param courseName
     * @param address
     * @param upTimeWeekStr
     * @param upTimeStr
     */
    private void appendText(StringBuilder sb, int no, String courseName, String address, String upTimeWeekStr, String upTimeStr) {
        sb.append("第" + no + "节课\n");
        sb.append(courseName + "\n");
        sb.append(address + "\n");
        sb.append(upTimeWeekStr + "\n");
    }

    /**
     * 追加信息
     *
     * @param course
     * @param tableText
     * @param userRobot
     */
    private void appendInfo(List<MessageInfo> course, String tableText, UserRobotDTO userRobot) {
        List<Robot> robotList = userRobot.getRobotList();
        if (!CollectionUtils.isEmpty(robotList)) {
            for (Robot robot : robotList) {
                MessageInfo mi = new MessageInfo();
                mi.setNum(robot.getTarget());
                mi.setType(QQType.valueOf(robot.getType()));
                mi.setMessage(tableText);
                course.add(mi);
            }
        }
    }
}
