package com.hjb.syllabus.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hjb.syllabus.entity.fields.CommonFields;
import com.hjb.syllabus.entity.fields.HTMLFields;
import com.hjb.syllabus.entity.fields.RedisFields;
import com.hjb.syllabus.entity.fields.UrlFields;
import com.hjb.syllabus.entity.po.JvtcUser;
import com.hjb.syllabus.entity.vo.ResponseVO;
import com.hjb.syllabus.service.JvtcUserService;
import com.hjb.syllabus.service.RedisService;
import com.hjb.syllabus.service.SyllabusService;
import com.hjb.syllabus.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.selector.Html;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author 胡江斌
 * @version 1.0
 * @title: SyllabusServiceImpl
 * @projectName syllabus
 * @description: TODO
 * @date 2020/5/28 12:23
 */
@Slf4j
@Service
public class SyllabusServiceImpl implements SyllabusService {

    public static Pattern pattern = Pattern.compile("第([0-9]+)周");

    @Resource
    private RedisService redisService;

    @Resource
    private JvtcUserService jvtcUserService;

    /**
     * 配置
     */
    public static Request.Builder request = new Request.Builder()
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
            .addHeader("Connection", "keep-alive")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Referer", "http://jiaowu.jvtc.jx.cn/jsxsd/")
            .addHeader("Upgrade-Insecure-Requests", "1")
            .addHeader("Host", "jiaowu.jvtc.jx.cn")
            .addHeader("Origin", "http://jiaowu.jvtc.jx.cn");

    public static final String LOGIN_TITLE_TEXT = "登录";

    /**
     * 获取课程表
     *
     * @param howWeeks 当前第几周(绝对周)
     * @param jvtcUser 用户
     * @param retryCount 重试次数
     * @return
     */
    @Override
    public Html getTimeTable(Integer howWeeks, JvtcUser jvtcUser, int retryCount) {

        // 当前第几周
        String url = UrlFields.KB_PAGE + "?rq=" + LocalDate.now().plusWeeks(howWeeks == null ? 0 : howWeeks - nowWeek()).format(DateTimeFormatter.ISO_LOCAL_DATE);
        // 有缓存就根据缓存获取
        if (!StringUtils.isEmpty(jvtcUser.getCookie())) {

            // 根据班级获取该班级的课表（本周）
            // 0表示当前周, 要进行设置
            howWeeks = howWeeks == 0 ? nowWeek() : howWeeks;
            String table = (String) redisService.get(String.format(RedisFields.TABLE, jvtcUser.getUsername(), howWeeks));
            if (!StringUtils.isEmpty(table)) {
                return Html.create(table);
            }
            request.header(UrlFields.COOKIE, jvtcUser.getCookie());
            // 判断缓存是否过期
            ResponseVO kbPageResponse = HttpUtil.get(url, request);
            Html kbHtml = Html.create(kbPageResponse.getHtml());
            // 成功
            if (isLogined(kbHtml)) {
                return processAndSaveToRedis(jvtcUser.getUsername(), howWeeks, kbHtml);
            }

        }
        // 未登录则进行登录,根据缓存获取页面失败了，表示缓存过期了
        ResponseVO loginResult = loginByUserNameAndEncode(jvtcUser.getUsername(), jvtcUser.getPassword());
        if (loginResult != null && loginResult.getCode() == HttpStatus.FOUND.value()) {
            request.header(UrlFields.COOKIE, loginResult.getHeaders().get(UrlFields.SET_COOKIE));
        }

        // 获取课表
        ResponseVO result = HttpUtil.get(url, request);
        Html html = Html.create(result.getHtml());
        if (result.getCode() == HttpStatus.OK.value() && isLogined(html)) {
            return processAndSaveToRedis(jvtcUser.getUsername(), howWeeks, html);
        }

        if (retryCount >= 1) {
            return getTimeTable(howWeeks, jvtcUser, --retryCount);
        }

        return Html.create(HTMLFields.GET_TABLE_FAIL);
    }

    /**
     * html页面处理<br>@
     * 将源页面修改为通用页面
     *
     * @param html  代码体
     */
    public Html processHtml(Html html) {

        try {
            Document doc = html.getDocument();
            doc.getElementsByTag("style").remove();
            doc.getElementsByTag("script").remove().toString();
            doc.select("table")
                    .removeAttr("class")
                    .addClass("layui-table")
                    .removeAttr("style")
                    // 将td补全
                    .select("td").select("p")
                    .forEach(element -> element.html(element.attr("title")));
            return html;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                return html;
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return Html.create("");
    }

    /**
     * 保存到redis
     *
     * @param id    学号（根据学号存入表格，格式[table::学号::相对几周])
     * @param weeks 相对几周
     * @param html  html代码
     */
    public void saveToRedis(String id, Integer weeks, String html) {
        // 插入redis
        if (!StringUtils.isEmpty(id)) {
            // 存入前清除\t\r\n
            redisService.set(String.format(RedisFields.TABLE, id, weeks == null ? 0 : weeks), html.replaceAll("[\t|\r|\n]", ""), CommonFields.ONE_DAY_SEC);
        }
    }

    private Html processAndSaveToRedis(String id, Integer weeks, Html html) {
        // 处理
        Html newHtml = processHtml(html);
        // 存入redis
        saveToRedis(id, weeks, newHtml.get());
        return newHtml;
    }

    /**
     * 进行登录
     *
     * @param username 用户名
     * @param encoded  压缩的密码
     * @return 如果返回401表示账号密码有误
     */
    @Override
    public ResponseVO loginByUserNameAndEncode(String username, String encoded) {


        // 非缓存登录(设置禁用临时重定向后一次就行)
        ResponseVO jvtcResponse = loginRequest(UrlFields.LOGIN_URL, username, encoded);
        String cookie = jvtcResponse.getHeaders().get(UrlFields.SET_COOKIE);
        if (StringUtils.isEmpty(cookie)) {
            return null;
        }

        // 清除cookie后其他内容
        cookie = cookie.split(";")[0];
        jvtcResponse.setCookie(cookie);

        // 插入或更新数据库缓存
        JvtcUser user = new JvtcUser();
        user.setUsername(username);
        Wrapper<JvtcUser> wrapper = new QueryWrapper<>(user);
        JvtcUser jvtcUser = jvtcUserService.getOne(wrapper);

        if (jvtcResponse != null &&
                (jvtcResponse.getCode() == HttpStatus.OK.value()
                        || jvtcResponse.getCode() == HttpStatus.FOUND.value()
                )) {
            // 插入/更新cookie
            if (jvtcUser == null) {
                jvtcUser = new JvtcUser();
                jvtcUser.setUsername(username);
                jvtcUser.setPassword(encoded);
                jvtcUser.setCookie(cookie);
                jvtcUserService.save(jvtcUser);
            } else {
                // 缓存不一致则更新
                if (!jvtcUser.getCookie().equals(cookie)) {
                    JvtcUser juser = new JvtcUser();
                    juser.setId(jvtcUser.getId());
                    juser.setCookie(cookie);
                    jvtcUserService.updateById(juser);
                }
                // 更新班级
                if (StringUtils.isEmpty(jvtcUser.getClazz())) {
                    JvtcUser juser = new JvtcUser();
                    juser.setId(jvtcUser.getId());

                    Request.Builder builder = SyllabusServiceImpl.request;
                    builder.addHeader(UrlFields.COOKIE, cookie);
                    ResponseVO userInfo = HttpUtil.get(UrlFields.INFO_PAGE, builder);
                    Html userInfoHtml = Html.create(userInfo.getHtml());
                    juser.setClazz(userInfoHtml.xpath("//div[@class='middletopttxlr']/div[6]/div[2]/text()").get());
                    jvtcUserService.updateById(juser);
                }
            }

            return jvtcResponse;
        }
        return new ResponseVO(401, null, null);
    }

    /**
     * 登录状态
     *
     * @param html 登录后的页面
     * @return
     */
    @Override
    public boolean isLogined(Html html) {
        if (html == null) {
            return false;
        }
        if (Objects.equals(html.getDocument().title(), LOGIN_TITLE_TEXT)) {
            return false;
        } else if (StringUtils.isEmpty(html.getDocument().tagName("tbody").text().trim())) {
            return false;
        } else if (html.get().contains("重新登录")) {
            return false;
        }
        return true;
    }

    /**
     * 登录状态
     *
     * @param cookie 缓存
     * @return
     */
    @Override
    public boolean isLogined(String cookie) {
        Request.Builder builder = request.addHeader(UrlFields.COOKIE, cookie);
        ResponseVO resp = HttpUtil.get(UrlFields.KB_PAGE, builder);
        if (isLogined(Html.create(resp.getHtml()))) {
            // session加入登录状态
            //SpringUtils.getCurrentSession().setAttribute(SessionFields.JVTC_COOKIE, cookie);
            return true;
        }
        return false;
    }

    /**
     * 进行登录请求
     *
     * @param username 用户名
     * @param encoded  加密数据
     * @return
     */
    public ResponseVO loginRequest(String username, String encoded) {
        return loginRequest(UrlFields.LOGIN_URL, username, encoded, null);
    }

    /**
     * 进行登录请求
     *
     * @param url      登录url
     * @param username 用户名
     * @param encoded  加密数据
     * @return
     */
    public ResponseVO loginRequest(String url, String username, String encoded) {
        return loginRequest(url, username, encoded, null);
    }

    /**
     * 进行登录请求
     *
     * @param url      登录url
     * @param username 用户名
     * @param encoded  加密数据
     * @return
     * @parma cookie 缓存
     */
    public ResponseVO loginRequest(String url, String username, String encoded, String cookie) {

        RequestBody body = new FormBody.Builder()
                .add("userAccount", username)
                .add("userPassword", "")
                .add("encoded", encoded)
                .build();
        Request.Builder builder = request;
        // 缓存存在则直接缓存登录
        if (!StringUtils.isEmpty(cookie)) {
            builder.addHeader(UrlFields.COOKIE, cookie);
        }

        return HttpUtil.post(url, builder, body);
    }

    /**
     * 获取当前周次
     *
     * @return
     */
    @Override
    public int nowWeek() {
        // 周次存入redis
        try {
            Object weeks = redisService.get(RedisFields.WEEK);
            return (Integer) weeks;
        } catch (NumberFormatException e) {
            log.error("获取周次失败", e);
        }
        return -1;
    }

}
