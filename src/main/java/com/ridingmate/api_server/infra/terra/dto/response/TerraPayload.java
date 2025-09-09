package com.ridingmate.api_server.infra.terra.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

public record TerraPayload(
        User user,
        List<Data> data,
        String type
) {

    public record User(
            @JsonProperty("user_id") String userId,
            String provider,
            @JsonProperty("reference_id") String referenceId
    ) {}

    public record Data(
            Metadata metadata,
            @JsonProperty("active_durations_data") ActiveDurationsData activeDurationsData,
            @JsonProperty("distance_data") DistanceData distanceData,
            @JsonProperty("position_data") PositionData positionData,
            @JsonProperty("movement_data") MovementData movementData
    ) {}

    public record Metadata(
            @JsonProperty("start_time") OffsetDateTime startTime,
            @JsonProperty("end_time") OffsetDateTime endTime,
            String name,
            int type
    ) {}

    public record ActiveDurationsData(
            @JsonProperty("activity_seconds") double activitySeconds
    ) {}

    public record DistanceData(
            Summary summary,
            Detailed detailed
    ) {
        public record Summary(
                @JsonProperty("distance_meters") double distanceMeters,
                Elevation elevation
        ) {}

        public record Elevation(
                @JsonProperty("gain_actual_meters") double gainActualMeters
        ) {}

        public record Detailed(
                @JsonProperty("elevation_samples") List<ElevationSample> elevationSamples,
                @JsonProperty("distance_samples") List<DistanceSample> distanceSamples
        ) {}
    }

    public record PositionData(
            @JsonProperty("position_samples") List<PositionSample> positionSamples
    ) {}

    public record ElevationSample(
            OffsetDateTime timestamp,
            @JsonProperty("elev_meters") double elevationMeters
    ) {}

    public record PositionSample(
            OffsetDateTime timestamp,
            @JsonProperty("coords_lat_lng_deg") List<Double> coordsLatLngDeg // [latitude, longitude]
    ) {}

    public record MovementData(
            Double avgSpeedMetersPerSecond,
            @JsonProperty("speed_samples") List<SpeedSample> speedSamples
    ){}

    public record SpeedSample(
            @JsonProperty("speed_meters_per_second") Double speedMetersPerSecond,
            OffsetDateTime timestamp,
            @JsonProperty("timer_duration_seconds") Long timerDurationSeconds
    ){}

    public record DistanceSample(
            @JsonProperty("distance_meters") Double distanceMeters,
            OffsetDateTime timestamp,
            @JsonProperty("timer_duration_seconds") Long timerDurationSeconds
    ) {}
}