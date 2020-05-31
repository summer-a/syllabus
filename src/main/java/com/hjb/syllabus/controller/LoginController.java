package com.hjb.syllabus.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hjb.syllabus.entity.fields.CommonFields;
import com.hjb.syllabus.entity.fields.SessionFields;
import com.hjb.syllabus.entity.po.JvtcUser;
import com.hjb.syllabus.entity.vo.ResponseVO;
import com.hjb.syllabus.service.JvtcUserService;
import com.hjb.syllabus.service.SyllabusService;
import com.hjb.syllabus.service.impl.SyllabusServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import us.codecraft.webmagic.selector.Html;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Objects;
import java.util.UUID;

/**
 * 用户登录/注册控制器
 *
 * @author 胡江斌
 * @version 1.0
 * @title: LoginController
 * @projectName blog
 * @description: TODO
 * @date 2019/6/26 14:10
 */
@Controller
@RequestMapping("/management")
public class LoginController {

    @Autowired
    private JvtcUserService jvtcUserService;

    @Autowired
    private SyllabusService syllabusService;

    /**
     * 跳转到职教云账号登录页
     *
     * @return
     */
    @GetMapping(value = "/page/login")
    public String icveLoginPage() {
        return "page/jvtcLogin";
    }

    /**
     * 九职教务处账号登录
     *
     * @param username   用户名
     * @param encoded    加密后的数据
     * @param rememberMe 是否记住用户
     * @return
     */
    @PostMapping(value = "/login")
    public String jvtcLogin(@RequestParam(value = "username", required = true) String username,
                            @RequestParam(value = "encoded", required = true) String encoded,
                            @RequestParam(value = "rememberMe", required = false, defaultValue = "false") boolean rememberMe,
                            HttpSession session,
                            HttpServletResponse response,
                            RedirectAttributes redirectAttributes) {
        // 判断用户是否已经有缓存
        JvtcUser t = new JvtcUser();
        t.setUsername(username);

        Wrapper<JvtcUser> wrapper = new QueryWrapper<>(t);
        JvtcUser jvtcUser = jvtcUserService.getOne(wrapper);

        if (jvtcUser == null || StringUtils.isEmpty(jvtcUser.getCookie())) {
            // 查询页面
            ResponseVO jResponse = syllabusService.loginByUserNameAndEncode(username, encoded);
            // 有则直接获取
            // 登录失败
            if (jResponse == null || Objects.equals(new Html(jResponse.getHtml()).getDocument().title(), SyllabusServiceImpl.LOGIN_TITLE_TEXT) || jResponse.getCode() == HttpStatus.UNAUTHORIZED.value()) {
                redirectAttributes.addFlashAttribute(SessionFields.MESSAGE, "登录失败，请检查用户名或密码");
                return "redirect:/management/page/login";
            }
            // 登录成功
            else {
                // 过期则清除缓存，判断数据库账号密码和提交的是否一致
                t.setPassword(encoded);
                Wrapper<JvtcUser> userWrapper = new QueryWrapper<>(t);
                JvtcUser jvtcUserOnline = jvtcUserService.getOne(userWrapper);
                // 不一致则更新密码
                if (jvtcUserOnline == null) {
                    t.setId(jvtcUser.getId());
                    jvtcUserService.updateById(t);
                }
                session.setAttribute(SessionFields.JVTC_USER, jvtcUserOnline);
                // 存入cookie和session
                String jvtcUserId = UUID.randomUUID().toString();
                session.setAttribute(SessionFields.JVTC_USER_ID, jvtcUserId);
                // 过期时间7天，单位=秒
                session.setMaxInactiveInterval(CommonFields.ONE_DAY_SEC * 7);
                Cookie jvtcUserIdCookie = new Cookie(SessionFields.JVTC_USER_ID, jvtcUserId);
                jvtcUserIdCookie.setMaxAge(CommonFields.ONE_DAY_SEC * 7);
                jvtcUserIdCookie.setPath("/");
                response.addCookie(jvtcUserIdCookie);
                return "redirect:/management/index";
            }

        } else {
            // 判断用户名密码是否正确
            if (username.equals(jvtcUser.getUsername()) && encoded.equals(jvtcUser.getPassword())) {
                // 判断cookie是否过期
                if (!StringUtils.isEmpty(jvtcUser.getCookie())) {
                    if (!syllabusService.isLogined(jvtcUser.getCookie())) {
                        // 过期，更新缓存
                        syllabusService.loginByUserNameAndEncode(username, encoded);
                    }
                    session.setAttribute(SessionFields.JVTC_USER, jvtcUser);
                    // 存入cookie和session
                    String jvtcUserId = UUID.randomUUID().toString();
                    session.setAttribute(SessionFields.JVTC_USER_ID, jvtcUserId);
                    // 过期时间7天，单位=秒
                    session.setMaxInactiveInterval(CommonFields.ONE_DAY_SEC * 7);
                    Cookie jvtcUserIdCookie = new Cookie("JVTC_USER_ID", jvtcUserId);
                    jvtcUserIdCookie.setMaxAge(CommonFields.ONE_DAY_SEC * 7);
                    jvtcUserIdCookie.setPath("/");
                    response.addCookie(jvtcUserIdCookie);

                    return "redirect:/management/index";
                }
            } else {
                redirectAttributes.addFlashAttribute(SessionFields.MESSAGE, "账号密码有误");
            }
        }

        return "redirect:/management/page/login";
    }

    @GetMapping(value = "/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(SessionFields.JVTC_USER);
        return "redirect:/management/page/login";
    }

}
