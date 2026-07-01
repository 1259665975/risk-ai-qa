package com.gm.riskaiRagent.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 轻量 BM25 打分，面向中文风控文档的关键词召回。
 */
public final class KeywordScorer {

    private static final double K1 = 1.2;
    private static final double B = 0.75;
    private static final double AVG_DOC_LEN = 400.0;

    private KeywordScorer() {
    }

    public static double bm25(String query, String document) {
        if (query == null || document == null || query.isBlank() || document.isBlank()) {
            return 0.0;
        }
        List<String> queryTerms = tokenize(query);
        if (queryTerms.isEmpty()) {
            return 0.0;
        }
        List<String> docTerms = tokenize(document);
        if (docTerms.isEmpty()) {
            return 0.0;
        }
        int docLen = docTerms.size();
        double score = 0.0;
        Set<String> seen = new HashSet<>();
        for (String term : queryTerms) {
            if (!seen.add(term)) {
                continue;
            }
            int tf = 0;
            for (String docTerm : docTerms) {
                if (term.equals(docTerm)) {
                    tf++;
                }
            }
            if (tf == 0) {
                continue;
            }
            double numerator = tf * (K1 + 1);
            double denominator = tf + K1 * (1 - B + B * docLen / AVG_DOC_LEN);
            score += numerator / denominator;
        }
        return score;
    }

    static List<String> tokenize(String text) {
        String normalized = text.toLowerCase(Locale.ROOT).trim();
        List<String> tokens = new ArrayList<>();
        StringBuilder cjkRun = new StringBuilder();

        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (isCjk(ch)) {
                cjkRun.append(ch);
                continue;
            }
            flushCjkRun(cjkRun, tokens);
            if (Character.isLetterOrDigit(ch)) {
                StringBuilder word = new StringBuilder();
                word.append(ch);
                while (i + 1 < normalized.length()) {
                    char next = normalized.charAt(i + 1);
                    if (!Character.isLetterOrDigit(next) || isCjk(next)) {
                        break;
                    }
                    word.append(next);
                    i++;
                }
                if (word.length() >= 2) {
                    tokens.add(word.toString());
                }
            }
        }
        flushCjkRun(cjkRun, tokens);
        return tokens;
    }

    private static void flushCjkRun(StringBuilder cjkRun, List<String> tokens) {
        if (cjkRun.isEmpty()) {
            return;
        }
        String run = cjkRun.toString();
        cjkRun.setLength(0);
        if (run.length() == 1) {
            tokens.add(run);
            return;
        }
        for (int i = 0; i < run.length() - 1; i++) {
            tokens.add(run.substring(i, i + 2));
        }
        if (run.length() <= 4) {
            tokens.add(run);
        }
    }

    private static boolean isCjk(char ch) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION;
    }
}
