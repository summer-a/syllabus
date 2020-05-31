package com.hjb.syllabus.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hjb.syllabus.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * 机器人对应用户表实体类
 * @author 胡江斌
 * @version 1.0
 * @title: Robot
 * @projectName blog
 * @description: TODO
 * @date 2019/7/5 23:20
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("robot")
public class Robot extends BaseEntity {

    @TableField("robot_name")
    private String robotName;

    @TableField("jvtc_user_id")
    private Integer jvtcUserId;

    @TableField("type")
    private String type;

    @TableField("target")
    private Long target;

    @TableField("remind_am")
    private LocalTime remindAm;

    @TableField("remind_pm")
    private LocalTime remindPm;

    @TableField("remind_eve")
    private LocalTime remindEve;

    @TableField("status")
    private Integer status;

    public Robot(Integer id) {
        super(id);
    }
}
