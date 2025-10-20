package com.ridingmate.api_server.domain.route.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "자연 경관 타입")
public enum LandscapeType {

    COASTAL("해안가", "바다와 해안선을 따라가는 경관"),
    ISLAND("섬", "도서 지역의 바다와 해안선을 감상할 수 있는 경관"),
    RIVERSIDE("강변", "강이나 하천을 따라가는 경관"),
    LAKE("호수", "호수나 저수지를 감상할 수 있는 경관"),
    FOREST("숲길", "나무가 우거진 숲길 경관"),
    VALLEY("계곡", "산골짜기와 계곡을 따라가는 경관"),
    MOUNTAIN("산", "산의 경치를 감상할 수 있는 경관"),
    FIELD("평야", "넓은 평야와 들판을 감상할 수 있는 경관"),
    COUNTRYSIDE("농촌", "시골길과 논밭이 어우러진 전원 풍경"),
    WETLAND("습지", "습지, 생태공원, 갈대밭 등 자연 서식지 중심 경관"),
    PARK("공원", "공원과 녹지를 감상할 수 있는 경관"),
    URBAN("도시", "도시의 스카이라인과 건물들을 감상할 수 있는 경관"),
    BRIDGE("다리", "아름다운 다리와 교량을 감상할 수 있는 경관"),
    DAM("댐", "댐과 인공호수를 감상할 수 있는 경관"),
    HISTORIC("역사문화", "유적지, 문화재, 고건축 등 역사적인 경관"),
    RURAL_HILL("언덕", "완만한 언덕과 구릉의 풍경이 조화된 경관");

    @Schema(description = "자연 경관 타입 이름")
    private final String displayName;

    @Schema(description = "자연 경관 타입 설명")
    private final String description;
}