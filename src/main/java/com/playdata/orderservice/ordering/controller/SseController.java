package com.playdata.orderservice.ordering.controller;

import com.playdata.orderservice.common.auth.TokenUserInfo;
import com.playdata.orderservice.ordering.dto.OrderingListResDto;
import com.playdata.orderservice.ordering.entity.Ordering;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class SseController {

    // 구독을 요청한 각 사용자의 이몌일을 키로 emitter 객체를 저장
    // ConcurrentHashMap : 멀티 스레드 기반 해시맵(HasMap은 싱글 스레드 기반)
    Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    
    
    @GetMapping("/subscribe")
    public SseEmitter subScribe(@AuthenticationPrincipal TokenUserInfo userInfo) {
        SseEmitter emitter = new SseEmitter(14400 * 60 * 1000L); //알림 서비스 구현 핵심 객체 (수명)
        String email = userInfo.getEmail();
        emitters.put(email, emitter); // 이메일을 키로 에미터를 저장

        log.info("subscribing to {} ", email);

        emitter.onCompletion(() -> emitters.remove(email)); // 클라이언트가 정상적으로 종려됐을 경우 맵에서 정보 없애기
        emitter.onTimeout(() -> emitters.remove(email)); // 설정한 수명이 지나면 맵에서 정보 없애기

        // 연결 성공 메세지 전송
        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));

            // 30초마다 heartbeat 메시지를 전송하여 연결 유지
            // 클라이언트에서 사용하는 EventSourcePolyfill이 45초동안 활동이 없으면 연결 종료
            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
                try {
                    emitter.send(SseEmitter.event().name("heartbeat").data("keep-alive"));
                } catch (IOException e) {
                    emitters.remove(email);
                    System.out.println("Failed to send heartbeat, removing emitter for email: " + email);
                }
            }, 30, 30, TimeUnit.SECONDS); // 30초마다 heartbeat 메시지 전송

        } catch (IOException e) {
          emitters.remove(email);
        }

        return emitter;
    }


    public void sendOrderMessage(Ordering save) {
        OrderingListResDto dto = save.fromEntity();
        SseEmitter emitter = emitters.get("admin@naver.com");
        try {
            emitter.send(SseEmitter.event().name("ordered").data(dto));
        } catch (IOException e) {
            emitters.remove("admin@naver.com");
        }
    }

}
