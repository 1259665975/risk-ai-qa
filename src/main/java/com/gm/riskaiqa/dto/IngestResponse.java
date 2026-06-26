package com.gm.riskaiqa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * {@code POST /doc/ingest} 响应体 DTO。
 * <p>返回入库文档的文件名、文档 ID、原始字符数、Token 数以及切片数。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestResponse implements Serializable {

    /** 上传的原始文件名。 */
    private String fileName;
    /** 入库生成的文档唯一标识。 */
    private String docId;
    /** 原始文本字符数。 */
    private int charCount;
    /** 按 jtokkit 计算的 Token 总数。 */
    private int tokenCount;
    /** 切片数量（800 Token/片，150 重叠）。 */
    private int chunkCount;
}
