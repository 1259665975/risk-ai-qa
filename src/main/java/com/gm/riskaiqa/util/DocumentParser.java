<<<<<<< HEAD
package com.gm.riskaiqa.util;

import com.gm.riskaiqa.common.BusinessException;
import com.gm.riskaiqa.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * Extracts plain text from uploaded documents (TXT / PDF / ...) using Apache Tika.
 * Tika auto-detects the content type, so PDF and plain text are handled uniformly.
 */
@Slf4j
@Component
public class DocumentParser {

    /** Allow large documents; -1 disables Tika's default 100k char write limit. */
    private final Tika tika;

    public DocumentParser() {
        this.tika = new Tika();
        this.tika.setMaxStringLength(-1);
    }

    public String parse(InputStream inputStream, String fileName) {
        try {
            String text = tika.parseToString(inputStream);
            return text == null ? "" : text.strip();
        } catch (IOException | TikaException e) {
            log.error("Failed to parse document: {}", fileName, e);
            throw new BusinessException(ResultCode.DOC_PARSE_ERROR,
                    "无法解析文档 [" + fileName + "]: " + e.getMessage());
        }
    }
}
=======
package com.gm.riskaiqa.util;

import com.gm.riskaiqa.common.BusinessException;
import com.gm.riskaiqa.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * Extracts plain text from uploaded documents (TXT / PDF / ...) using Apache Tika.
 * Tika auto-detects the content type, so PDF and plain text are handled uniformly.
 */
@Slf4j
@Component
public class DocumentParser {

    /** Allow large documents; -1 disables Tika's default 100k char write limit. */
    private final Tika tika;

    public DocumentParser() {
        this.tika = new Tika();
        this.tika.setMaxStringLength(-1);
    }

    public String parse(InputStream inputStream, String fileName) {
        try {
            String text = tika.parseToString(inputStream);
            return text == null ? "" : text.strip();
        } catch (IOException | TikaException e) {
            log.error("Failed to parse document: {}", fileName, e);
            throw new BusinessException(ResultCode.DOC_PARSE_ERROR,
                    "无法解析文档 [" + fileName + "]: " + e.getMessage());
        }
    }
}
>>>>>>> 7998cf43f5debde367904ed821ec8539275331cc
