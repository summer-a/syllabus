package com.hjb.syllabus.entity.fields;

/**
 * 常用链接
 *
 * @author 胡江斌
 * @version 1.0
 * @title: UrlFields
 * @projectName blog
 * @description: TODO
 * @date 2019/12/16 18:24
 */
public final class UrlFields {

    /**
     * 处理页面<br>
     * 参数rq=日期
     */
    public static final String KB_PAGE = "http://jiaowu.jvtc.jx.cn/jsxsd/framework/main_index_loadkb.jsp";
    /**
     * 周次获取
     */
    public static final String GET_NOW_WEEK = "http://jiaowu.jvtc.jx.cn/jsxsd/framework/xsMain_new.jsp";
    /**
     * 登录连接
     */
    public static final String LOGIN_URL = "http://jiaowu.jvtc.jx.cn/jsxsd/xk/LoginToXk";
    /**
     * 主页，包含用户信息
     */
    public static final String INFO_PAGE = "http://jiaowu.jvtc.jx.cn/jsxsd/framework/xsMain_new.jsp";
    /**
     * cookie
     */
    public static final String COOKIE = "Cookie";
    /**
     * Set-Cookie
     */
    public static final String SET_COOKIE = "Set-Cookie";
    /**
     * 强制刷新链接
     */
    public static final String TABLE_REFRESH = "/kb/%s/%s/true";

    private UrlFields() {

    }
}
