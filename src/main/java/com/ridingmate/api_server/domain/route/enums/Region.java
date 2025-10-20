package com.ridingmate.api_server.domain.route.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "지역")
@Getter
@RequiredArgsConstructor
public enum Region {
    SEOUL("서울", "서울특별시"),
    INCHEON("인천", "인천광역시"),
    GYEONGGI("경기", "경기도"),
    GANGWON("강원", "강원특별자치도"),
    DAEJEON("대전", "대전광역시"),
    SEJONG("세종", "세종특별자치시"),
    CHUNGBUK("충북", "충청북도"),
    CHUNGNAM("충남", "충청남도"),
    DAEGU("대구", "대구광역시"),
    GYEONGBUK("경북", "경상북도"),
    GWANGJU("광주", "광주광역시"),
    JEONBUK("전북", "전북특별자치도"),
    JEONNAM("전남", "전라남도"),
    BUSAN("부산", "부산광역시"),
    ULSAN("울산", "울산광역시"),
    GYEONGNAM("경남", "경상남도"),
    JEJU("제주", "제주특별자치도"),
    ETC("그 외", "그 외 지역");

    private final String displayName;
    private final String description;
}