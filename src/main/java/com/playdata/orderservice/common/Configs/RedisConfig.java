package com.playdata.orderservice.common.Configs;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

// 레디스 db연동을 위한 객체
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;


    // Redis 서버와의 연결을 설정하는 역할을 하는 RedisConnectionFactory
    // Redis 접속에 필요한 설정을 지정한 후 구현체를 빈으로 등록
    @Bean
    @Qualifier("user-redis-factory") 
    // redis의 경우에는 16개의 db 제공 각 db마다 서로 다른 데이터 저장 가능
    // redis에 접속을 하는 객체는 RedisConnectionFactory -> 같은 타입의 빈이 여러개 등록될 수 있다.
    // 같은 타입의 빈을 구분할 수 있도록 @Qualifier를 붙혀 구분
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(0); // 0번 db를 사용하겠다. default = 0

        return new LettuceConnectionFactory(configuration);

    }

    
    // RedisTemplate은 redi와 상호 작용할 때 redis key, value의 형식을 정의
    @Bean
    @Qualifier("user-template")
    public RedisTemplate<String, Object> redisTemplate(
            @Qualifier("user-redis-factory") RedisConnectionFactory factory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // Redis의 키를 문자열로 직렬화 시키겠다.
        template.setKeySerializer(new StringRedisSerializer());

        // value는 JSON 형태로 직렬화 시키겠다.
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(factory);

        return template;
    }

}
