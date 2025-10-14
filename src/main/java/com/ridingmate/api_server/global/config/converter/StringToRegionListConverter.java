package com.ridingmate.api_server.global.config.converter;

import com.ridingmate.api_server.domain.route.enums.Region;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * String을 Region List로 변환하는 Converter
 * URL 파라미터에서 쉼표로 구분된 문자열을 Region List로 변환
 */
@Component
public class StringToRegionListConverter implements Converter<String, List<Region>> {
    
    @Override
    public List<Region> convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Arrays.stream(source.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Region::valueOf)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            // 잘못된 Enum 값이 들어온 경우 null 반환
            return null;
        }
    }
} 