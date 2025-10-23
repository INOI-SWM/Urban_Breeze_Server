package com.ridingmate.api_server.infra.aws.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridingmate.api_server.domain.activity.service.TerraWebhookProcessingService;
import com.ridingmate.api_server.infra.terra.TerraErrorCode;
import com.ridingmate.api_server.infra.terra.TerraException;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class TerraSqsConsumer {

    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final TerraWebhookProcessingService terraWebhookProcessingService;

    @SqsListener("${cloud.aws.sqs.terra-queue-name}")
    public void receiveMessage(String payload) {
        try {
            log.info("SQS로부터 Terra 메시지 수신");
            log.debug("수신 페이로드: {}", payload);

            JsonNode rootNode = objectMapper.readTree(payload);
            String type = rootNode.path("type").asText();

            String finalPayload = payload;
            String originalType = type;

            if ("s3_payload".equals(type)) {
                String downloadUrl = rootNode.path("url").asText();
                log.info("S3 페이로드 감지, URL에서 데이터 다운로드 시작: {}", downloadUrl);

                try {
                    // 대용량 페이로드를 위한 스트리밍 처리
                    finalPayload = webClient.get()
                            .uri(URI.create(downloadUrl))
                            .retrieve()
                            .bodyToMono(String.class)
                            .timeout(java.time.Duration.ofMinutes(5)) // 5분 타임아웃
                            .block();

                    if (finalPayload == null || finalPayload.isEmpty()) {
                        log.error("S3에서 페이로드를 다운로드하지 못했습니다. URL: {}", downloadUrl);
                        throw new TerraException(TerraErrorCode.TERRA_PAYLOAD_DOWNLOAD_FAILED);
                    }

                    log.info("S3 페이로드 다운로드 완료");
                    log.debug("다운로드한 S3 페이로드: {}", finalPayload);
                    originalType = objectMapper.readTree(finalPayload).path("type").asText();
                    
                } catch (WebClientResponseException e) {
                    if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                        log.warn("Terra S3 URL 만료로 인한 403 Forbidden 에러 (메시지 무시): URL={}, error={}", 
                                downloadUrl, e.getMessage());
                        log.info("만료된 Terra 요청을 건너뜀 - 새로운 요청을 기다립니다.");
                        return; // 메시지 처리를 중단하고 정상적으로 종료
                    } else if (e.getStatusCode().is4xxClientError()) {
                        log.error("Terra S3 URL 클라이언트 에러 (4xx): URL={}, status={}, error={}", 
                                downloadUrl, e.getStatusCode(), e.getMessage());
                        throw new TerraException(TerraErrorCode.TERRA_S3_URL_CLIENT_ERROR);
                    } else if (e.getStatusCode().is5xxServerError()) {
                        log.error("Terra S3 URL 서버 에러 (5xx): URL={}, status={}, error={}", 
                                downloadUrl, e.getStatusCode(), e.getMessage());
                        throw new TerraException(TerraErrorCode.TERRA_S3_URL_SERVER_ERROR);
                    } else {
                        log.error("Terra S3 URL 기타 HTTP 에러: URL={}, status={}, error={}", 
                                downloadUrl, e.getStatusCode(), e.getMessage());
                        throw new TerraException(TerraErrorCode.TERRA_PAYLOAD_DOWNLOAD_FAILED);
                    }
                } catch (Exception e) {
                    log.error("Terra S3 URL 다운로드 중 예상치 못한 에러: URL={}, error={}", 
                            downloadUrl, e.getMessage(), e);
                    throw new TerraException(TerraErrorCode.TERRA_PAYLOAD_DOWNLOAD_FAILED);
                }
            }

            // 타입에 따라 적절한 서비스 메소드 호출
            switch (originalType) {
                case "auth":
                    log.info("Auth 이벤트 처리 시작");
                    terraWebhookProcessingService.processAuthEvent(finalPayload);
                    break;
                case "activity":
                    log.info("Activity 이벤트 처리 시작");
                    terraWebhookProcessingService.processActivityEvent(finalPayload);
                    break;
                case "body":
                case "daily":
                case "sleep":
                    log.info("{} 이벤트 수신. 현재는 처리 로직이 구현되지 않았습니다.", originalType);
                    //TODO 기타 활동 처리
                    break;
                case "user_reauth":
                    log.warn("사용자 재인증 필요 이벤트 수신: {}", finalPayload);
                    break;
                default:
                    log.warn("알 수 없는 Terra 이벤트 타입 수신: '{}'", originalType);
                    break;
            }

        } catch (JsonProcessingException e) {
            log.error("Terra SQS 메시지 파싱 실패: {}", payload, e);
            // JSON 파싱 실패는 복구 불가능하므로 메시지가 DLQ로 가도록 예외를 다시 던짐
            throw new RuntimeException("JSON parsing failed", e);
        } catch (Exception e) {
            log.error("Terra SQS 메시지 처리 중 예상치 못한 오류 발생: {}", payload, e);
            // 기타 모든 예외도 DLQ로 보내 재처리 시도 또는 수동 분석
            throw new RuntimeException("Unexpected error during SQS message processing", e);
        }
    }
}
