package com.ridingmate.api_server.domain.route.dto.request;

import com.ridingmate.api_server.domain.route.enums.Difficulty;
import com.ridingmate.api_server.domain.route.enums.LandscapeType;
import com.ridingmate.api_server.domain.route.enums.RecommendationType;
import com.ridingmate.api_server.domain.route.enums.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GpxUploadRequest(
        @Schema(description = "추천코스 제목", example = "한강 라이딩 코스")
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String title,

        @Schema(description = "추천코스 설명", example = "한강을 따라가는 아름다운 라이딩 코스입니다.")
        @Size(max = 500, message = "설명은 500자를 초과할 수 없습니다.")
        String description,

        @Schema(description = "난이도", example = "MEDIUM", allowableValues = {"EASY", "MEDIUM", "HARD"})
        @NotNull(message = "난이도는 필수입니다.")
        Difficulty difficulty,

        @Schema(description = "지역", example = "SEOUL", allowableValues = {"SEOUL", "GYEONGGI", "ETC"})
        @NotNull(message = "지역은 필수입니다.")
        Region region,

        @Schema(description = "경관 타입", example = "RIVERSIDE", allowableValues = {"RIVERSIDE", "MOUNTAIN", "CITY", "COASTAL"})
        @NotNull(message = "경관 타입은 필수입니다.")
        LandscapeType landscapeType,

        @Schema(description = "추천 타입", example = "FAMOUS", allowableValues = {"FAMOUS", "POPULAR", "SCENIC", "CHALLENGING"})
        @NotNull(message = "추천 타입은 필수입니다.")
        RecommendationType recommendationType
) {}
