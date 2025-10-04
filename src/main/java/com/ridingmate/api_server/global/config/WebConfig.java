package com.ridingmate.api_server.global.config;

import com.ridingmate.api_server.global.config.converter.StringToDifficultyListConverter;
import com.ridingmate.api_server.global.config.converter.StringToLandscapeTypeListConverter;
import com.ridingmate.api_server.global.config.converter.StringToRecommendationTypeListConverter;
import com.ridingmate.api_server.global.config.converter.StringToRegionListConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${swagger.server-url}")
    private String serverUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(serverUrl)
                .allowedMethods("*")
                .allowCredentials(true);

        registry.addMapping("/v3/api-docs/**")
                .allowedOrigins(serverUrl)
                .allowedMethods("*")
                .allowCredentials(true);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Enum List Converter 등록
        registry.addConverter(new StringToLandscapeTypeListConverter());
        registry.addConverter(new StringToRecommendationTypeListConverter());
        registry.addConverter(new StringToRegionListConverter());
        registry.addConverter(new StringToDifficultyListConverter());
    }

    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        resolver.setResolveLazily(true);
        return resolver;
    }
}