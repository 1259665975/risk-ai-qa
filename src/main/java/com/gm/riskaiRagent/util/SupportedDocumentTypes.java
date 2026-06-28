package com.gm.riskaiRagent.util;

import com.gm.riskaiRagent.common.BusinessException;
import com.gm.riskaiRagent.common.ResultCode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 知识库支持的文件类型定义与校验。
 */
public final class SupportedDocumentTypes {

    private SupportedDocumentTypes() {
    }

    /** 纯文本 / 标记语言。 */
    public static final Set<String> TEXT = Set.of(
            "txt", "md", "markdown", "csv", "json", "xml", "html", "htm", "log"
    );

    /** PDF。 */
    public static final Set<String> PDF = Set.of("pdf");

    /** Word / 富文本。 */
    public static final Set<String> WORD = Set.of("doc", "docx", "rtf", "odt");

    /** Excel 表格。 */
    public static final Set<String> EXCEL = Set.of("xls", "xlsx", "ods");

    /** PowerPoint 演示文稿。 */
    public static final Set<String> PRESENTATION = Set.of("ppt", "pptx", "odp");

    /** 电子书等。 */
    public static final Set<String> OTHER = Set.of("epub");

    /** 图片（走视觉 OCR）。 */
    public static final Set<String> IMAGE = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "tif", "tiff"
    );

    private static final Set<String> ALL = Set.of(
            "txt", "md", "markdown", "csv", "json", "xml", "html", "htm", "log",
            "pdf",
            "doc", "docx", "rtf", "odt",
            "xls", "xlsx", "ods",
            "ppt", "pptx", "odp",
            "epub",
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "tif", "tiff"
    );

    public static boolean isSupported(String fileName) {
        String ext = extension(fileName);
        return ext != null && ALL.contains(ext);
    }

    public static boolean isImage(String fileName) {
        String ext = extension(fileName);
        return ext != null && IMAGE.contains(ext);
    }

    public static String extension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public static void validate(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "文件名不能为空");
        }
        if (!isSupported(fileName)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(),
                    "不支持的文件类型 [" + fileName + "]，允许格式: " + String.join(", ", allExtensions()));
        }
    }

    public static List<String> allExtensions() {
        return ALL.stream().sorted().collect(Collectors.toList());
    }

    public static Map<String, Object> toResponseMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("text", TEXT);
        map.put("pdf", PDF);
        map.put("word", WORD);
        map.put("excel", EXCEL);
        map.put("presentation", PRESENTATION);
        map.put("image", IMAGE);
        map.put("other", OTHER);
        map.put("all", allExtensions());
        map.put("accept", allExtensions().stream()
                .map(ext -> "." + ext)
                .collect(Collectors.joining(",")));
        map.put("description", "TXT/MD/CSV、PDF、Word(DOC/DOCX)、Excel、PPT、图片(JPG/PNG等，视觉OCR)、EPUB");
        return map;
    }
}
