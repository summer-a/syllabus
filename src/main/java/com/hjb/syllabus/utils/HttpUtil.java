package com.hjb.syllabus.utils;

import com.hjb.syllabus.entity.vo.ResponseVO;
import com.xiaoleilu.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.http.HttpStatus;

import java.io.IOException;

/**
 * @author 胡江斌
 * @version 1.0
 * @title: HttpUtils
 * @projectName blog
 * @description: TODO
 * @date 2019/8/31 14:26
 */
@Slf4j
public class HttpUtil {

    /**
     * okhttp客户端
     */
    private static OkHttpClient client = new OkHttpClient();


    /**
     * get请求
     *
     * @param url
     * @param builder
     * @return
     */
    public static ResponseVO get(String url, Request.Builder builder) {
        // 请求
        Request request = builder.url(url).build();
        // 响应
        try (Response response = client.newCall(request).execute()) {
            return new ResponseVO(response.code(), response.body().string(), response.headers());
        } catch (IOException e) {
            log.error("请求异常", e);
            return new ResponseVO(500, null, null);
        }
    }

    /**
     * post 请求
     *
     * @param url     地址
     * @param builder 信息
     * @param body    主体信息
     * @return
     * @throws IOException
     */
    public static ResponseVO post(String url, Request.Builder builder, RequestBody body) {
        Request request = builder.url(url).post(body).build();
        try (Response response = client.newBuilder().followRedirects(false).build().newCall(request).execute()) {
            return new ResponseVO(response.code(), response.body().string(), response.headers());
        } catch (IOException e) {
            log.error("请求异常", e);
            return new ResponseVO(500, null, null);
        }
    }

    /**
     * get请求
     *
     * @param url
     * @param builder
     * @return
     */
    public static JSONObject getReturnJson(String url, Request.Builder builder) {
        ResponseVO response = get(url, builder);
        if (response.getCode() == HttpStatus.OK.value()) {
            return new JSONObject(response.getHtml());
        }
        return new JSONObject();
    }

    /**
     * post 请求
     *
     * @param url     地址
     * @param builder 信息
     * @param body    主体信息
     * @return
     * @throws IOException
     */
    public static JSONObject postReturnJson(String url, Request.Builder builder, RequestBody body) {
        ResponseVO response = post(url, builder, body);
        if (response.getCode() == HttpStatus.OK.value()) {
            return new JSONObject(response.getHtml());
        }
        return new JSONObject();
    }

}
