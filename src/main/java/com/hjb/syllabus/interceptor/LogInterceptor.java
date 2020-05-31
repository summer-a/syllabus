package com.hjb.syllabus.interceptor;

import com.hjb.syllabus.entity.po.JvtcUser;
import com.hjb.syllabus.entity.fields.SessionFields;
import com.hjb.syllabus.utils.SpringUtil;
import com.xiaoleilu.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 日志拦截器
 * @author 胡江斌
 * @version 1.0
 * @title: LogInterceptor
 * @projectName blog
 * @description: TODO
 * @date 2019/7/19 12:13
 */
@Slf4j
public class LogInterceptor extends HandlerInterceptorAdapter {

    /**
     * This implementation always returns {@code true}.
     *
     * @param request
     * @param response
     * @param handler
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 请求开始时间
        request.setAttribute("request_start_time", System.currentTimeMillis());
        return true;
    }

    /**
     * This implementation is empty.
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 获取请求开始时间
        Long startTime = (Long) request.getAttribute("request_start_time");
        // 请求结束时间
        long endTime = System.currentTimeMillis();
        log.info("本次请求耗时：" + (endTime - startTime) + "毫秒；" + getRequestInfo(request).toString());
    }

    /**
     * 主要功能:获取请求详细信息
     * 注意事项:无
     *
     * @param request 请求
     * @return 请求信息
     */
    private StringBuilder getRequestInfo(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        StringBuilder reqInfo = new StringBuilder();
        reqInfo.append(" 请求路径=" + servletPath);

        String userName = "[游客]";
        try {
            HttpSession currentSession = SpringUtil.getCurrentSession();
            JvtcUser jvtcUser = null;
            if (currentSession != null) {
                jvtcUser = (JvtcUser) currentSession.getAttribute(SessionFields.JVTC_USER);
            }
            if (jvtcUser != null) {
                userName = String.format("[%s]", jvtcUser.getUsername());
            }
        } catch (Exception e) {
        }
        reqInfo.append(" 操作人=" + userName);
        Map<String, String[]> parameterMap = request.getParameterMap();
        reqInfo.append(" 请求参数=" + new JSONObject(parameterMap).toString());

        return reqInfo;
    }
}
