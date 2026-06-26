package com.gm.riskaiqa.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gm.riskaiqa.common.BusinessException;
import com.gm.riskaiqa.common.ResultCode;
import com.gm.riskaiqa.dto.IngestResponse;
import com.gm.riskaiqa.dto.PageResult;
import com.gm.riskaiqa.entity.SysCategory;
import com.gm.riskaiqa.entity.SysDocument;
import com.gm.riskaiqa.mapper.SysCategoryMapper;
import com.gm.riskaiqa.mapper.SysDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortalDocumentService {

    private final SysDocumentMapper sysDocumentMapper;
    private final SysCategoryMapper sysCategoryMapper;
    private final DocumentService documentService;

    public PageResult<SysDocument> page(int page, int size, Long categoryId, String keyword) {
        Page<SysDocument> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysDocument> wrapper = new LambdaQueryWrapper<SysDocument>()
                .orderByDesc(SysDocument::getId);
        if (categoryId != null) {
            wrapper.eq(SysDocument::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysDocument::getFileName, keyword);
        }
        Page<SysDocument> result = sysDocumentMapper.selectPage(pageParam, wrapper);
        fillCategoryNames(result.getRecords());
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    public SysDocument upload(MultipartFile file, Long categoryId) throws IOException {
        if (categoryId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "请选择分类");
        }
        if (sysCategoryMapper.selectById(categoryId) == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "分类不存在");
        }

        IngestResponse ingest = documentService.ingest(file, categoryId);
        SysDocument doc = new SysDocument();
        doc.setDocId(ingest.getDocId());
        doc.setFileName(ingest.getFileName());
        doc.setCategoryId(categoryId);
        doc.setFileSize(file.getSize());
        doc.setCharCount(ingest.getCharCount());
        doc.setTokenCount(ingest.getTokenCount());
        doc.setChunkCount(ingest.getChunkCount());
        doc.setStatus("SUCCESS");
        sysDocumentMapper.insert(doc);
        fillCategoryNames(List.of(doc));
        return doc;
    }

    public void delete(Long id) {
        SysDocument doc = sysDocumentMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        documentService.deleteByDocId(doc.getDocId());
        sysDocumentMapper.deleteById(id);
    }

    private void fillCategoryNames(List<SysDocument> docs) {
        if (docs == null || docs.isEmpty()) {
            return;
        }
        List<Long> categoryIds = docs.stream()
                .map(SysDocument::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (categoryIds.isEmpty()) {
            return;
        }
        Map<Long, String> nameMap = new HashMap<>();
        for (SysCategory category : sysCategoryMapper.selectBatchIds(categoryIds)) {
            nameMap.put(category.getId(), category.getName());
        }
        for (SysDocument doc : docs) {
            if (doc.getCategoryId() != null) {
                doc.setCategoryName(nameMap.get(doc.getCategoryId()));
            }
        }
    }
}
