package com.hjb.syllabus.interceptor;

import com.hjb.syllabus.utils.TimeTableUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 公用资源拦截器
 *
 * @author 胡江斌
 * @version 1.0
 * @title: CommonResourceHandler
 * @projectName blog
 * @description: TODO
 * @date 2019/6/12 22:54
 */
public class CommonResourceHandler extends HandlerInterceptorAdapter {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    /**
     * This implementation is empty.
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

    /**
     * 项目销毁
     */
    @PreDestroy
    public void destroy() {
        // 关闭任务计划线程池
        TimeTableUtil.task.shutdownNow();
    }

}
