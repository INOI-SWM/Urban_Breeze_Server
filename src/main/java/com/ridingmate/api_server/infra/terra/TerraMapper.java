package com.ridingmate.api_server.infra.terra;

import com.ridingmate.api_server.domain.activity.dto.response.IntegrationAuthenticateResponse;
import com.ridingmate.api_server.domain.activity.dto.response.IntegrationProviderAuthResponse;
import com.ridingmate.api_server.infra.terra.dto.response.TerraGenerateAuthLinkResponse;
import com.ridingmate.api_server.infra.terra.dto.response.TerraProviderAuthResponse;
import com.ridingmate.api_server.infra.terra.dto.response.TerraPayload;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class TerraMapper {
    public static IntegrationAuthenticateResponse toIntegrationAuthenticateResponse(TerraGenerateAuthLinkResponse response){
        if (response == null) {
            return null;
        }
        return new IntegrationAuthenticateResponse(response.url());
    }

    public static IntegrationProviderAuthResponse toIntegrationProviderAuthResponse(TerraProviderAuthResponse response){
        if (response == null) {
            return null;
        }
        return new IntegrationProviderAuthResponse(response.authUrl());
    }

    /**
     * Terra PositionSample을 JTS Coordinate 배열로 변환
     * @param terraData Terra 활동 데이터
     * @return JTS Coordinate 배열 (longitude, latitude, elevation 순서)
     */
    public Coordinate[] toCoordinates(TerraPayload.Data terraData) {
        List<TerraPayload.PositionSample> positionSamples = extractPositionSamples(terraData);
        
        if (positionSamples.isEmpty()) {
            return new Coordinate[0];
        }
        
        return positionSamples.stream()
                .map(this::positionSampleToCoordinate)
                .filter(Objects::nonNull)
                .toArray(Coordinate[]::new);
    }

    /**
     * Terra 데이터에서 Position 샘플 추출
     * @param terraData Terra 활동 데이터
     * @return Position 샘플 리스트
     */
    private List<TerraPayload.PositionSample> extractPositionSamples(TerraPayload.Data terraData) {
        return terraData.positionData() != null 
                ? terraData.positionData().positionSamples() 
                : Collections.emptyList();
    }

    /**
     * Terra PositionSample을 JTS Coordinate로 변환
     * @param pos Terra Position 샘플
     * @return JTS Coordinate (longitude, latitude, elevation)
     */
    private Coordinate positionSampleToCoordinate(TerraPayload.PositionSample pos) {
        List<Double> coords = pos.coordsLatLngDeg();
        if (coords == null || coords.size() < 2) {
            return null;
        }
        
        // Terra 좌표: [latitude, longitude] 순서
        // JTS Coordinate: (longitude, latitude, elevation) 순서로 변환
        double latitude = coords.get(0);
        double longitude = coords.get(1);
        double elevation = 0.0; // 기본값, 나중에 고도 데이터 추가 가능
        
        return new Coordinate(longitude, latitude, elevation);
    }
}
