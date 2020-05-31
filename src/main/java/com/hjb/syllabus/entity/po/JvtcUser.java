package com.hjb.syllabus.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hjb.syllabus.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 九职教务处账号信息
 * @author 胡江斌
 * @version 1.0
 * @title: JvtcUser
 * @projectName blog
 * @description: TODO
 * @date 2019/6/25 21:28
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("jvtc_user")
public class JvtcUser extends BaseEntity {

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("truename")
    private String truename;

    @TableField("vip")
    private Boolean vip;

    @TableField("duration_end_time")
    private LocalDateTime durationEndTime;

    @TableField("clazz")
    private String clazz;

    @TableField("cookie")
    private String cookie;

    public JvtcUser(Integer id) {
        super(id);
    }

}
