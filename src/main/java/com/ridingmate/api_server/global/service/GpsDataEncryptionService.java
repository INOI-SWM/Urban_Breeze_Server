package com.ridingmate.api_server.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 기존 GPS 데이터 암호화 마이그레이션 서비스 (대용량 데이터 최적화)
 * 주의: 한 번만 실행해야 합니다!
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GpsDataEncryptionService {

    private final GpsDataEncryptionBatchService batchService;
    
    private static final int BATCH_SIZE = 1000; // 배치 크기
    private static final int LOG_INTERVAL = 10000; // 로그 출력 주기

    /**
     * 모든 GPS 데이터 암호화
     */
    public void encryptAllGpsData() {
        log.info("=== 기존 GPS 데이터 암호화 시작 ===");
        long startTime = System.currentTimeMillis();
        
        encryptActivityGpsLogs();
        encryptRouteGpsLogs();
        
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;
        log.info("=== 기존 GPS 데이터 암호화 완료: {}초 소요 ===", duration);
    }

    /**
     * ActivityGpsLog 암호화 (Activity 단위)
     */
    public void encryptActivityGpsLogs() {
        log.info("========================================");
        log.info("[ActivityGpsLog] 암호화 시작");
        log.info("========================================");
        
        long startTime = System.currentTimeMillis();
        long totalCount = batchService.getActivityCount();
        
        log.info("[ActivityGpsLog] 암호화 대상 Activity: {}개", String.format("%,d", totalCount));
        
        if (totalCount == 0) {
            log.info("[ActivityGpsLog] 암호화할 데이터 없음");
            return;
        }
        
        AtomicLong processed = new AtomicLong(0);
        AtomicLong failed = new AtomicLong(0);
        AtomicLong processedActivities = new AtomicLong(0);
        long minActivityId = 0;
        int batchCount = 0;
        
        // Activity 단위로 처리
        while (true) {
            batchCount++;
            long batchStartTime = System.currentTimeMillis();
            
            try {
                log.info(">>> 배치 #{} 시작: minActivityId={}, 배치크기={}", 
                        batchCount, minActivityId, 50);
                
                int batchResult = batchService.processActivityBatch(
                    minActivityId, 50, processed, failed  // 50개 Activity씩 처리
                );
                
                long batchElapsed = System.currentTimeMillis() - batchStartTime;
                log.info("<<< 배치 #{} 완료: 처리된 Activity={}, 소요시간={}ms", 
                        batchCount, batchResult, batchElapsed);
                
                if (batchResult == 0) {
                    log.info("[ActivityGpsLog] 더 이상 처리할 데이터 없음. 종료.");
                    break; // 더 이상 데이터 없음
                }
                
                processedActivities.addAndGet(batchResult);
                minActivityId += 1000; // 다음 배치를 위한 Activity ID 증가
                
                // 진행 상황 로그 (매 배치마다)
                double progress = (processedActivities.get() * 100.0) / totalCount;
                long totalElapsed = (System.currentTimeMillis() - startTime) / 1000;
                log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                log.info("📊 진행률: {}/{} Activity ({} %)", 
                        String.format("%,d", processedActivities.get()), 
                        String.format("%,d", totalCount), 
                        String.format("%.1f", progress));
                log.info("📦 암호화된 GPS 로그: {} 개", String.format("%,d", processed.get()));
                log.info("❌ 실패: {} 개", failed.get());
                log.info("⏱️  경과 시간: {}초 ({}분)", totalElapsed, totalElapsed / 60);
                log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                
                // 메모리 정리
                System.gc();
                Thread.sleep(100);
                
            } catch (Exception e) {
                log.error("❌ [ActivityGpsLog] 배치 #{} 처리 실패: minActivityId={}", batchCount, minActivityId, e);
                failed.incrementAndGet();
                minActivityId += 1000;
            }
        }
        
        long totalElapsed = (System.currentTimeMillis() - startTime) / 1000;
        log.info("========================================");
        log.info("✅ [ActivityGpsLog] 암호화 완료!");
        log.info("   - 처리된 Activity: {} 개", String.format("%,d", processedActivities.get()));
        log.info("   - 암호화된 GPS 로그: {} 개", String.format("%,d", processed.get()));
        log.info("   - 실패: {} 개", failed.get());
        log.info("   - 총 소요시간: {}초 ({}분)", totalElapsed, totalElapsed / 60);
        log.info("========================================");
    }

    /**
     * RouteGpsLog 암호화 (Route 단위)
     */
    public void encryptRouteGpsLogs() {
        log.info("========================================");
        log.info("[RouteGpsLog] 암호화 시작");
        log.info("========================================");
        
        long startTime = System.currentTimeMillis();
        long totalCount = batchService.getRouteCount();
        
        log.info("[RouteGpsLog] 암호화 대상 Route: {}개", String.format("%,d", totalCount));
        
        if (totalCount == 0) {
            log.info("[RouteGpsLog] 암호화할 데이터 없음");
            return;
        }
        
        AtomicLong processed = new AtomicLong(0);
        AtomicLong failed = new AtomicLong(0);
        AtomicLong processedRoutes = new AtomicLong(0);
        long minRouteId = 0;
        int batchCount = 0;
        
        while (true) {
            batchCount++;
            long batchStartTime = System.currentTimeMillis();
            
            try {
                log.info(">>> 배치 #{} 시작: minRouteId={}, 배치크기={}", 
                        batchCount, minRouteId, 50);
                
                int batchResult = batchService.processRouteBatch(
                    minRouteId, 50, processed, failed  // 50개 Route씩 처리
                );
                
                long batchElapsed = System.currentTimeMillis() - batchStartTime;
                log.info("<<< 배치 #{} 완료: 처리된 Route={}, 소요시간={}ms", 
                        batchCount, batchResult, batchElapsed);
                
                if (batchResult == 0) {
                    log.info("[RouteGpsLog] 더 이상 처리할 데이터 없음. 종료.");
                    break;
                }
                
                processedRoutes.addAndGet(batchResult);
                minRouteId += 1000;
                
                // 진행 상황 로그 (매 배치마다)
                double progress = (processedRoutes.get() * 100.0) / totalCount;
                long totalElapsed = (System.currentTimeMillis() - startTime) / 1000;
                log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                log.info("📊 진행률: {}/{} Route ({} %)", 
                        String.format("%,d", processedRoutes.get()), 
                        String.format("%,d", totalCount), 
                        String.format("%.1f", progress));
                log.info("📦 암호화된 GPS 로그: {} 개", String.format("%,d", processed.get()));
                log.info("❌ 실패: {} 개", failed.get());
                log.info("⏱️  경과 시간: {}초 ({}분)", totalElapsed, totalElapsed / 60);
                log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                
                System.gc();
                Thread.sleep(100);
                
            } catch (Exception e) {
                log.error("❌ [RouteGpsLog] 배치 #{} 처리 실패: minRouteId={}", batchCount, minRouteId, e);
                failed.incrementAndGet();
                minRouteId += 1000;
            }
        }
        
        long totalElapsed = (System.currentTimeMillis() - startTime) / 1000;
        log.info("========================================");
        log.info("✅ [RouteGpsLog] 암호화 완료!");
        log.info("   - 처리된 Route: {} 개", String.format("%,d", processedRoutes.get()));
        log.info("   - 암호화된 GPS 로그: {} 개", String.format("%,d", processed.get()));
        log.info("   - 실패: {} 개", failed.get());
        log.info("   - 총 소요시간: {}초 ({}분)", totalElapsed, totalElapsed / 60);
        log.info("========================================");
    }
}
