package com.ridingmate.api_server.domain.privacy.entity;

import com.ridingmate.api_server.domain.privacy.enums.LocationAccessType;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 위치정보 조회 기록 엔티티
 * 위치정보보호법 제16조에 따른 위치정보 수집·이용·제공 기록 보존
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "location_data_access_logs", indexes = {
        @Index(name = "idx_location_access_user_id", columnList = "user_id"),
        @Index(name = "idx_location_access_accessed_at", columnList = "accessed_at"),
        @Index(name = "idx_location_access_access_type", columnList = "access_type")
})
public class LocationDataAccessLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false)
    private LocationAccessType accessType;

    @Column(name = "accessed_at", nullable = false)
    private LocalDateTime accessedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "data_type", nullable = false)
    private String dataType; // "ROUTE_GPS", "ACTIVITY_GPS", "GPX_FILE"

    @Column(name = "data_id")
    private String dataId; // Route ID, Activity ID, File ID

    @Column(name = "purpose", length = 200)
    private String purpose; // "ROUTE_VIEW", "ACTIVITY_VIEW", "GPX_DOWNLOAD"

    @Column(name = "retention_period_days")
    private Integer retentionPeriodDays = 1095; // 3년 (1095일)

    @Builder
    private LocationDataAccessLog(User user, LocationAccessType accessType, LocalDateTime accessedAt,
                                  String ipAddress, String userAgent, String dataType, String dataId,
                                  String purpose, Integer retentionPeriodDays) {
        this.user = user;
        this.accessType = accessType;
        this.accessedAt = accessedAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.dataType = dataType;
        this.dataId = dataId;
        this.purpose = purpose;
        this.retentionPeriodDays = retentionPeriodDays;
    }

    /**
     * 위치정보 조회 기록 생성
     */
    public static LocationDataAccessLog createAccessLog(User user, LocationAccessType accessType,
                                                       String ipAddress, String userAgent,
                                                       String dataType, String dataId, String purpose) {
        return LocationDataAccessLog.builder()
                .user(user)
                .accessType(accessType)
                .accessedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .dataType(dataType)
                .dataId(dataId)
                .purpose(purpose)
                .build();
    }

    /**
     * 보존기간 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(accessedAt.plusDays(retentionPeriodDays));
    }
}
