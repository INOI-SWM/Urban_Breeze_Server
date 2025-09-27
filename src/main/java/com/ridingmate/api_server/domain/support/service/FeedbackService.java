package com.ridingmate.api_server.domain.support.service;

import com.ridingmate.api_server.domain.support.dto.request.CreateFeedbackRequest;
import com.ridingmate.api_server.domain.support.dto.response.FeedbackResponse;
import com.ridingmate.api_server.domain.support.entity.Feedback;
import com.ridingmate.api_server.domain.support.repository.FeedbackRepository;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserService userService;

    /**
     * 피드백 생성
     */
    @Transactional
    public FeedbackResponse createFeedback(Long userId, CreateFeedbackRequest request) {
        log.info("피드백 생성 시작: userId={}", userId);

        User user = userService.getUser(userId);

        Feedback feedback = Feedback.builder()
                .user(user)
                .content(request.content())
                .build();

        Feedback savedFeedback = feedbackRepository.save(feedback);

        log.info("피드백 생성 완료: feedbackId={}", savedFeedback.getId());
        return FeedbackResponse.from(savedFeedback);
    }
}