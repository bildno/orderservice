package com.playdata.orderservice.common.Configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CORS(Cross-Origin Resource Sharing)
// CORS : 웹 어플리케이션이 다른 도메인에서 리소르를 요청할 때 발생하는 보안 문제를 해결하기 위해 사용

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://orderservice-front-1113.s3-website.ap-northeast-2.amazonaws.com")
                .allowedMethods("*") // get, post 요청 등 허용 여부
                .allowedHeaders("*")
                .allowCredentials(true); // 보안처리 할 건지 ex(쿠키에 세션값을 담는 등)

    }

}
