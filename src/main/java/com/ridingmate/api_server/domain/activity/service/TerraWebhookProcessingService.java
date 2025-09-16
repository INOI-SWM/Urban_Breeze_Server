package com.ridingmate.api_server.domain.activity.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridingmate.api_server.domain.activity.entity.Activity;
import com.ridingmate.api_server.domain.activity.entity.ActivityGpsLog;
import com.ridingmate.api_server.domain.activity.facade.ActivityFacade;
import com.ridingmate.api_server.domain.activity.repository.ActivityGpsLogRepository;
import com.ridingmate.api_server.domain.activity.repository.ActivityRepository;
import com.ridingmate.api_server.domain.user.entity.TerraUser;
import com.ridingmate.api_server.domain.user.exception.TerraUserErrorCode;
import com.ridingmate.api_server.domain.user.exception.TerraUserException;
import com.ridingmate.api_server.domain.user.repository.TerraUserRepository;
import com.ridingmate.api_server.infra.terra.dto.response.TerraPayload;
import com.ridingmate.api_server.infra.terra.TerraActivityType;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TerraWebhookProcessingService {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final ActivityGpsLogRepository activityGpsLogRepository;
    private final TerraUserRepository terraUserRepository;
    private final ActivityFacade activityFacade;

    @Transactional
    public void processAuthEvent(String payload) throws JsonProcessingException {
        TerraPayload terraPayload = objectMapper.readValue(payload, TerraPayload.class);
        TerraPayload.User user = terraPayload.user();

        if (user == null || user.referenceId() == null) {
            log.error("Auth 이벤트에 user 객체 또는 reference_id가 없습니다: {}", payload);
            return;
        }

        TerraUser terraUser = terraUserRepository.findByTerraUserId(UUID.fromString(user.userId()))
                .orElseThrow(() -> new TerraUserException(TerraUserErrorCode.TERRA_USER_NOT_FOUND));

        terraUser.setActive();
    }

    @Transactional
    public void processActivityEvent(String payload) throws JsonProcessingException {
        TerraPayload terraPayload = objectMapper.readValue(payload, TerraPayload.class);
        if (terraPayload.user() == null || terraPayload.user().referenceId() == null) {
            log.warn("[Terra] Activity 이벤트에 user 정보 또는 reference_id가 없어 처리를 건너뜁니다.");
            return;
        }

        User user = userRepository.findByUuid(UUID.fromString(terraPayload.user().referenceId()))
                .orElseThrow(() -> {
                    log.error("[Terra] Activity의 reference_id에 해당하는 유저를 찾을 수 없습니다: {}", terraPayload.user().referenceId());
                    return new RuntimeException("유저를 찾을 수 없습니다.");
                });

        List<TerraPayload.Data> cyclingActivities = terraPayload.data().stream()
                .filter(data -> TerraActivityType.isCyclingActivity(data.metadata().type()))
                .toList();

        if (CollectionUtils.isEmpty(cyclingActivities)) {
            log.info("[Terra] 처리할 자전거 활동 데이터가 없습니다. (들어온 활동 타입: {})",
                    terraPayload.data().stream().map(d -> d.metadata().type()).toList());
            return;
        }

        for (TerraPayload.Data activityData : cyclingActivities) {
            // ActivityFacade를 통해 Activity 생성 (썸네일 포함)
            Activity newActivity = activityFacade.createActivityFromTerraData(user, activityData, activityData);
            
            // GPS 로그 생성 및 저장
            List<ActivityGpsLog> gpsLogs = createActivityGpsLogEntities(newActivity, activityData);
            if (!CollectionUtils.isEmpty(gpsLogs)) {
                activityGpsLogRepository.saveAll(gpsLogs);
            }
            
            log.info("[Terra] 활동 저장 완료 (썸네일 포함): activityId={}, gpsLogCount={}, thumbnailPath={}", 
                    newActivity.getId(), gpsLogs.size(), newActivity.getThumbnailImagePath());
        }
    }


    private List<ActivityGpsLog> createActivityGpsLogEntities(Activity activity, TerraPayload.Data data) {
        List<TerraPayload.PositionSample> positionSamples = data.positionData() != null ? data.positionData().positionSamples() : Collections.emptyList();
        List<TerraPayload.ElevationSample> elevationSamples = data.distanceData().detailed() != null ? data.distanceData().detailed().elevationSamples() : Collections.emptyList();
        List<TerraPayload.SpeedSample> speedSamples = data.movementData().speedSamples() != null ? data.movementData().speedSamples() : Collections.emptyList();
        List<TerraPayload.DistanceSample> distanceSamples = data.distanceData().detailed().distanceSamples() != null ? data.distanceData().detailed().distanceSamples() : Collections.emptyList();

        if (CollectionUtils.isEmpty(positionSamples)) {
            return Collections.emptyList();
        }

        NavigableMap<OffsetDateTime, Double> elevationMap = elevationSamples.stream()
                .collect(Collectors.toMap(
                    TerraPayload.ElevationSample::timestamp,
                    TerraPayload.ElevationSample::elevationMeters,
                    (e1, e2) -> e1, // 중복 키의 경우 첫 번째 값을 사용
                    TreeMap::new
                ));

        NavigableMap<OffsetDateTime, Double> speedMap = speedSamples.stream()
                .collect(Collectors.toMap(
                        TerraPayload.SpeedSample::timestamp,
                        TerraPayload.SpeedSample::speedMetersPerSecond,
                        (e1, e2) -> e1,
                        TreeMap::new
                ));

        NavigableMap<OffsetDateTime, Double> distanceMap = distanceSamples.stream()
                .collect(Collectors.toMap(
                        TerraPayload.DistanceSample::timestamp,
                        TerraPayload.DistanceSample::distanceMeters,
                        (e1, e2) -> e1,
                        TreeMap::new
                ));




        return positionSamples.stream()
                .map(pos -> {
                    List<Double> coords = pos.coordsLatLngDeg();
                    if (coords == null || coords.size() < 2) return null;

                    Double elevation = findClosestData(pos.timestamp(), elevationMap);
                    Double speed = findClosestData(pos.timestamp(), speedMap);
                    Double distance = findClosestData(pos.timestamp(), distanceMap);

                    return ActivityGpsLog.builder()
                            .activity(activity)
                            .logTime(pos.timestamp().toLocalDateTime())
                            .latitude(coords.get(0))
                            .longitude(coords.get(1))
                            .elevation(elevation)
                            .speed(speed)
                            .distance(distance)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Double findClosestData(OffsetDateTime targetTime, NavigableMap<OffsetDateTime, Double> dataMap) {
        if (dataMap.isEmpty()) {
            return null;
        }
        // 타임스탬프와 가장 가까운 키(시간)를 찾음
        Map.Entry<OffsetDateTime, Double> floorEntry = dataMap.floorEntry(targetTime);
        Map.Entry<OffsetDateTime, Double> ceilingEntry = dataMap.ceilingEntry(targetTime);

        if (floorEntry == null && ceilingEntry == null) return null;
        if (floorEntry == null) return ceilingEntry.getValue();
        if (ceilingEntry == null) return floorEntry.getValue();

        // 두 시간과의 차이를 계산하여 더 가까운 쪽의 고도 값을 반환
        long diffToFloor = Duration.between(floorEntry.getKey(), targetTime).abs().toMillis();
        long diffToCeiling = Duration.between(ceilingEntry.getKey(), targetTime).abs().toMillis();

        return diffToFloor < diffToCeiling ? floorEntry.getValue() : ceilingEntry.getValue();
    }
}
