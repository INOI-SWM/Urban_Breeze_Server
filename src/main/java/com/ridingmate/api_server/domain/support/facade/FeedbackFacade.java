package com.ridingmate.api_server.domain.support.facade;

import com.ridingmate.api_server.domain.support.dto.request.CreateFeedbackRequest;
import com.ridingmate.api_server.domain.support.dto.response.FeedbackResponse;
import com.ridingmate.api_server.domain.support.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 피드백 관련 Facade
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeedbackFacade {

    private final FeedbackService feedbackService;

    /**
     * 피드백 생성
     */
    public FeedbackResponse createFeedback(Long userId, CreateFeedbackRequest request) {
        log.info("피드백 생성 Facade 시작: userId={}", userId);
        return feedbackService.createFeedback(userId, request);
    }
}