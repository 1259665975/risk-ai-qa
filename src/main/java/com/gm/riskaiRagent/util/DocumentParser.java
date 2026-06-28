package com.gm.riskaiRagent.util;

import com.gm.riskaiRagent.common.BusinessException;
import com.gm.riskaiRagent.common.ResultCode;
import com.gm.riskaiRagent.service.ImageTextExtractorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 从上传文件中提取纯文本：Apache Tika 解析办公文档，图片走百炼视觉 OCR。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentParser {

    private final ImageTextExtractorService imageTextExtractorService;
    private final AutoDetectParser autoDetectParser = new AutoDetectParser();

    public ParseResult parse(byte[] bytes, String fileName) {
        SupportedDocumentTypes.validate(fileName);
        if (bytes == null || bytes.length == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "上传文件不能为空");
        }

        String ext = SupportedDocumentTypes.extension(fileName);
        if (SupportedDocumentTypes.isImage(fileName)) {
            String text = imageTextExtractorService.extract(bytes, fileName);
            return new ParseResult(text, "VISION_OCR", ext);
        }

        String text = parseWithTika(bytes, fileName);
        if (text.isBlank()) {
            throw new BusinessException(ResultCode.DOC_PARSE_ERROR,
                    "文档解析结果为空 [" + fileName + "]，请确认文件未加密且内容可读");
        }
        return new ParseResult(text, "TIKA", ext);
    }

    private String parseWithTika(byte[] bytes, String fileName) {
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, fileName);
            autoDetectParser.parse(inputStream, handler, metadata, new ParseContext());
            String text = handler.toString();
            return text == null ? "" : text.strip();
        } catch (IOException | SAXException | TikaException e) {
            log.error("Failed to parse document: {}", fileName, e);
            throw new BusinessException(ResultCode.DOC_PARSE_ERROR,
                    "无法解析文档 [" + fileName + "]: " + e.getMessage());
        }
    }

    public record ParseResult(String text, String parseMode, String fileType) {
    }
}
