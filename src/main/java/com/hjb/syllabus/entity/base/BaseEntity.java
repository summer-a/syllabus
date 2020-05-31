package com.hjb.syllabus.entity.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * @author 胡江斌
 * @version 1.0
 * @title: BaseEntity
 * @projectName blog
 * @description: TODO
 * @date 2019/6/8 14:14
 */
@Data
@NoArgsConstructor
public class BaseEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    public BaseEntity(Integer id) {
        this.id = id;
    }

}
