package com.gm.riskaiRagent.service;

import com.gm.riskaiRagent.common.BusinessException;
import com.gm.riskaiRagent.common.ResultCode;
import com.gm.riskaiRagent.config.RagProperties;
import com.gm.riskaiRagent.util.SupportedDocumentTypes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.util.Locale;
import java.util.Set;

/**
 * 使用百炼视觉模型（qwen-vl）从图片中提取文字与关键信息，供 RAG 入库。
 */
@Slf4j
@Service
public class ImageTextExtractorService {

    private static final Set<String> EMPTY_OCR_MARKERS = Set.of(
            "无文字内容", "无文本内容", "no text", "no text content"
    );

    private final ChatModel chatModel;
    private final RagProperties ragProperties;

    public ImageTextExtractorService(@Lazy ChatModel chatModel, RagProperties ragProperties) {
        this.chatModel = chatModel;
        this.ragProperties = ragProperties;
    }

    public String extract(byte[] imageBytes, String fileName) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new BusinessException(ResultCode.DOC_PARSE_ERROR, "图片内容为空");
        }
        String ext = SupportedDocumentTypes.extension(fileName);
        MimeType mimeType = mimeTypeOf(ext);
        String prompt = ragProperties.getDocument().getOcrPrompt();
        if (prompt == null || prompt.isBlank()) {
            prompt = "请提取图片中的全部文字，只输出识别内容。";
        }

        try {
            Media media = new Media(mimeType, new ByteArrayResource(imageBytes));
            UserMessage userMessage = UserMessage.builder()
                    .text(prompt)
                    .media(media)
                    .build();

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model(ragProperties.getDocument().getVisionModel())
                    .temperature(0.1)
                    .build();

            String text = chatModel.call(new Prompt(userMessage, options))
                    .getResult()
                    .getOutput()
                    .getText();
            String normalized = text == null ? "" : text.strip();
            if (normalized.isBlank() || isEmptyOcrResult(normalized)) {
                throw new BusinessException(ResultCode.DOC_PARSE_ERROR,
                        "图片未识别到可用文字内容 [" + fileName + "]");
            }
            log.info("Image OCR done: file={}, chars={}", fileName, normalized.length());
            return normalized;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Image OCR failed: {}", fileName, e);
            throw new BusinessException(ResultCode.DOC_PARSE_ERROR,
                    "图片识别失败 [" + fileName + "]: " + e.getMessage());
        }
    }

    private boolean isEmptyOcrResult(String text) {
        String compact = text.strip();
        if (EMPTY_OCR_MARKERS.contains(compact)) {
            return true;
        }
        return EMPTY_OCR_MARKERS.contains(compact.toLowerCase(Locale.ROOT));
    }

    private MimeType mimeTypeOf(String ext) {
        if (ext == null) {
            return MimeTypeUtils.IMAGE_JPEG;
        }
        return switch (ext) {
            case "png" -> MimeTypeUtils.IMAGE_PNG;
            case "gif" -> MimeTypeUtils.parseMimeType("image/gif");
            case "bmp" -> MimeTypeUtils.parseMimeType("image/bmp");
            case "webp" -> MimeTypeUtils.parseMimeType("image/webp");
            case "tif", "tiff" -> MimeTypeUtils.parseMimeType("image/tiff");
            default -> MimeTypeUtils.IMAGE_JPEG;
        };
    }
}
