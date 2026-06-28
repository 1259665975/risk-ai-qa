package com.gm.riskaiRagent.controller;

import com.gm.riskaiRagent.common.Result;
import com.gm.riskaiRagent.common.ResultCode;
import com.gm.riskaiRagent.dto.IngestResponse;
import com.gm.riskaiRagent.service.DocumentService;
import com.gm.riskaiRagent.util.SupportedDocumentTypes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 知识库文档 控制器。
 * <p>接收上传的知识库文档（TXT/PDF/Office/图片等）并解析入库，或清空整个知识库。
 * 所有接口路径以 {@code /doc} 开头。</p>
 */
@Tag(name = "Document", description = "知识库文档解析入库 / 清空")
@RestController
@RequestMapping("/doc")
@RequiredArgsConstructor
public class DocController {

    private final DocumentService documentService;

    @Operation(summary = "解析并入库文档（TXT/PDF/Word/Excel/PPT/图片等，Tika + 视觉 OCR）")
    @PostMapping(value = "/ingest", consumes = "multipart/form-data")
    public Result<IngestResponse> ingest(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), "上传文件不能为空");
        }
        return Result.success(documentService.ingest(file));
    }

    @Operation(summary = "查询支持的文档格式列表")
    @GetMapping("/supported-types")
    public Result<Map<String, Object>> supportedTypes() {
        return Result.success(SupportedDocumentTypes.toResponseMap());
    }

    @Operation(summary = "清空知识库（删除 Milvus 中所有已入库向量）")
    @DeleteMapping("/clear")
    public Result<Map<String, Object>> clear() {
        long removed = documentService.clearAll();
        return Result.success(Map.of("removedChunks", removed));
    }
}
