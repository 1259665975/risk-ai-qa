package com.gm.riskaiqa.controller;

import com.gm.riskaiqa.common.Result;
import com.gm.riskaiqa.common.ResultCode;
import com.gm.riskaiqa.dto.IngestResponse;
import com.gm.riskaiqa.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 知识库文档 控制器。
 * <p>接收上传的 TXT/PDF 文档并解析入库，或清空整个知识库。
 * 所有接口路径以 {@code /doc} 开头。</p>
 */
@Tag(name = "Document", description = "知识库文档解析入库 / 清空")
@RestController
@RequestMapping("/doc")
@RequiredArgsConstructor
public class DocController {

    private final DocumentService documentService;

    @Operation(summary = "解析并入库文档（TXT/PDF，Tika 解析 + 800/150 Token 切片）")
    @PostMapping(value = "/ingest", consumes = "multipart/form-data")
    public Result<IngestResponse> ingest(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), "上传文件不能为空");
        }
        return Result.success(documentService.ingest(file));
    }

    @Operation(summary = "清空知识库（删除 Milvus 中所有已入库向量）")
    @DeleteMapping("/clear")
    public Result<Map<String, Object>> clear() {
        long removed = documentService.clearAll();
        return Result.success(Map.of("removedChunks", removed));
    }
}
