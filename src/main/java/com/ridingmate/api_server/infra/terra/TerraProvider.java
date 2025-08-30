package com.ridingmate.api_server.infra.terra;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "테라 지원 서비스 목록")
public enum TerraProvider {
    STRAVA,
    GARMIN
    ;
}
