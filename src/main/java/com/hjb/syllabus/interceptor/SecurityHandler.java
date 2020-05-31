package com.hjb.syllabus.interceptor;

import com.hjb.syllabus.entity.po.JvtcUser;
import com.hjb.syllabus.entity.fields.SessionFields;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Objects;

/**
 * 管理页面安全处理
 * @author 胡江斌
 * @version 1.0
 * @title: SecurityHandler
 * @projectName blog
 * @description: TODO
 * @date 2019/8/16 8:32
 */
public class SecurityHandler extends HandlerInterceptorAdapter {

    /**
     * This implementation always returns {@code true}.
     *
     * @param request
     * @param response
     * @param handler
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        JvtcUser user = (JvtcUser) session.getAttribute(SessionFields.JVTC_USER);
        if (user != null) {
            String jvtcUserId = session.getAttribute(SessionFields.JVTC_USER_ID) + "";
            // 判断cookie和session是否对应
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (Objects.equals(cookie.getName(), SessionFields.JVTC_USER_ID) && Objects.equals(cookie.getValue(), jvtcUserId)) {
                        return true;
                    }
                }
            }
            // 如果没有JVTC_USER_ID的cookie和session,清除掉用户
            session.removeAttribute(SessionFields.JVTC_USER);
            session.removeAttribute(SessionFields.JVTC_USER_ID);
        }
        response.sendRedirect("/management/page/login");
        return false;
    }
}
