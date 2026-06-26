package com.gm.riskaiqa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_document")
public class SysDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String docId;
    private String fileName;
    private Long categoryId;
    private Long fileSize;
    private Integer charCount;
    private Integer tokenCount;
    private Integer chunkCount;
    private String status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String categoryName;
}
