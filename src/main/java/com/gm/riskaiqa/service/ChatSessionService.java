package com.gm.riskaiqa.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gm.riskaiqa.common.BusinessException;
import com.gm.riskaiqa.common.ResultCode;
import com.gm.riskaiqa.dto.ChatRequest;
import com.gm.riskaiqa.dto.QaRequest;
import com.gm.riskaiqa.dto.QaResponse;
import com.gm.riskaiqa.dto.ReferenceChunk;
import com.gm.riskaiqa.entity.ChatMessage;
import com.gm.riskaiqa.entity.ChatSession;
import com.gm.riskaiqa.mapper.ChatMessageMapper;
import com.gm.riskaiqa.mapper.ChatSessionMapper;
import com.gm.riskaiqa.security.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final RagQaService ragQaService;
    private final ObjectMapper objectMapper;

    public List<ChatSession> listSessions() {
        Long userId = AuthContext.userId();
        List<ChatSession> sessions = chatSessionMapper.selectList(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .orderByDesc(ChatSession::getUpdatedAt));
        for (ChatSession session : sessions) {
            Long count = chatMessageMapper.selectCount(new LambdaQueryWrapper<ChatMessage>()
                    .eq(ChatMessage::getSessionId, session.getId()));
            session.setMessageCount(count == null ? 0 : count.intValue());
        }
        return sessions;
    }

    public ChatSession createSession(String title) {
        ChatSession session = new ChatSession();
        session.setUserId(AuthContext.userId());
        session.setTitle(StringUtils.hasText(title) ? title : "新对话");
        chatSessionMapper.insert(session);
        session.setMessageCount(0);
        return session;
    }

    public void deleteSession(Long id) {
        ChatSession session = requireOwnedSession(id);
        chatSessionMapper.deleteById(session.getId());
    }

    public List<ChatMessageVO> listMessages(Long sessionId) {
        requireOwnedSession(sessionId);
        return chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getId))
                .stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    public QaResponse chat(ChatRequest request) {
        ChatSession session = requireOwnedSession(request.getSessionId());
        QaRequest qaRequest = new QaRequest();
        qaRequest.setQuestion(request.getQuestion());
        qaRequest.setIncludeReferences(request.isIncludeReferences());
        QaResponse response = ragQaService.ask(qaRequest, request.getCategoryIds());

        ChatMessage message = new ChatMessage();
        message.setSessionId(session.getId());
        message.setQuestion(request.getQuestion());
        message.setAnswer(response.getAnswer());
        message.setReferenceDocs(toJson(response.getReferences()));
        message.setFromCache(response.isFromCache() ? 1 : 0);
        message.setDegraded(response.isDegraded() ? 1 : 0);
        message.setCostMs(response.getCostMs());
        chatMessageMapper.insert(message);

        if ("新对话".equals(session.getTitle())) {
            session.setTitle(truncate(request.getQuestion(), 30));
            chatSessionMapper.updateById(session);
        }

        return response;
    }

    private ChatSession requireOwnedSession(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "sessionId 不能为空");
        }
        ChatSession session = chatSessionMapper.selectById(id);
        if (session == null || !AuthContext.userId().equals(session.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "会话不存在");
        }
        return session;
    }

    private ChatMessageVO toVo(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setQuestion(message.getQuestion());
        vo.setAnswer(message.getAnswer());
        vo.setReferences(readReferences(message.getReferenceDocs()));
        vo.setFromCache(message.getFromCache() != null && message.getFromCache() == 1);
        vo.setDegraded(message.getDegraded() != null && message.getDegraded() == 1);
        vo.setCostMs(message.getCostMs());
        return vo;
    }

    private List<ReferenceChunk> readReferences(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<ReferenceChunk>>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String toJson(Object obj) {
        try {
            return obj == null ? null : objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }

    @lombok.Data
    public static class ChatMessageVO {
        private String question;
        private String answer;
        private List<ReferenceChunk> references;
        private boolean fromCache;
        private boolean degraded;
        private long costMs;
    }
}
