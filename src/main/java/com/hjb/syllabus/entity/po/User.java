package com.hjb.syllabus.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hjb.syllabus.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("user")
public class User extends BaseEntity {

    /**
     * 用户名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 用户密码
     */
    @TableField("user_pass")
    private String userPass;

    /**
     * 用户昵称
     */
    @TableField("user_nickname")
    private String userNickname;

    /**
     * 用户邮箱
     */
    @TableField("user_email")
    private String userEmail;

    /**
     * 用户url
     */
    @TableField("user_url")
    private String userUrl;

    /**
     * 用户头像
     */
    @TableField("user_avatar")
    private String userAvatar;

    /**
     * 用户最后登录ip
     */
    @TableField("user_last_login_ip")
    private String userLastLoginIp;

    /**
     * 用户状态
     */
    @TableField("user_status")
    private Integer userStatus;

    /**
     * 用户最后登录时间
     */
    @TableField("user_last_login_time")
    private LocalDateTime userLastLoginTime;

}