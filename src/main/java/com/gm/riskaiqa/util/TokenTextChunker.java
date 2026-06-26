package com.gm.riskaiqa.util;

import com.gm.riskaiqa.config.RagProperties;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.IntArrayList;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits text into token-bounded chunks using a BPE tokenizer (jtokkit).
 * <p>Rule: each chunk holds at most {@code chunk.size} tokens (default 800) and
 * consecutive chunks overlap by {@code chunk.overlap} tokens (default 150),
 * i.e. a sliding window with step = size - overlap.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenTextChunker {

    private final RagProperties ragProperties;
    private Encoding encoding;

    @PostConstruct
    public void init() {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        EncodingType type;
        try {
            type = EncodingType.valueOf(ragProperties.getChunk().getEncoding().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown encoding '{}', fallback to CL100K_BASE",
                    ragProperties.getChunk().getEncoding());
            type = EncodingType.CL100K_BASE;
        }
        this.encoding = registry.getEncoding(type);
    }

    /** Counts the number of tokens in the given text. */
    public int countTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return encoding.countTokens(text);
    }

    /**
     * Splits the text into overlapping token chunks.
     *
     * @return non-empty chunk strings in order
     */
    public List<String> split(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        int size = Math.max(1, ragProperties.getChunk().getSize());
        int overlap = Math.max(0, Math.min(ragProperties.getChunk().getOverlap(), size - 1));
        int step = size - overlap;

        IntArrayList tokens = encoding.encode(text);
        int total = tokens.size();
        if (total <= size) {
            chunks.add(text);
            return chunks;
        }

        for (int start = 0; start < total; start += step) {
            int end = Math.min(start + size, total);
            IntArrayList window = new IntArrayList(end - start);
            for (int i = start; i < end; i++) {
                window.add(tokens.get(i));
            }
            String chunk = encoding.decode(window).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            if (end == total) {
                break;
            }
        }
        return chunks;
    }
}
