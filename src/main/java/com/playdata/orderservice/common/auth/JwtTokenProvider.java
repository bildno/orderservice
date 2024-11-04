package com.playdata.orderservice.common.auth;

import com.playdata.orderservice.user.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
// 토큰을 발급하고 서명위조를 검사하는 객체
public class JwtTokenProvider {

    // 서명에 사용할 값 (512비트 이상의 랜덤 문자열을 권장)
    // yml에 있는 값 가져오기 (properties 방식으로 선언)
    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private int expiration;
    
    /*
            {
                "iss": "서비스 이름(발급자)",
                "exp": "2023-12-27(만료일자)",
                "iat": "2023-11-27(발급일자)",
                "email": "로그인한 사람 이메일",
                "role": "Premium"
                ...
                == 서명
            }
         */

    // 토큰 생성 메서드
    public String createToken(String email, String role) {

        // Claims : 페이로드에 들어갈 사용자 정보
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        // 만료시간 지정을 위한 date 객체
        Date date = new Date();


        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(date)
                // 현재 시간 밀리초에 30분을 더한 시간을 만료시간으로 세팅
                .setExpiration(new Date(date.getTime() + expiration * 60 * 1000L))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }


    /**
     * 클라이언트가 전송한 토큰을 디코딩하여 토큰의 위조 여부를 확인
     * 토큰을 json으로 파싱해서 클레임(토큰 정보)을 리턴
     *
     * @param token - 필터가 전달해 준 토큰
     * @return - 토큰 안에 있는 인증된 유저 정보를 반환
     */
    public TokenUserInfo validateAndGetTokenUserInfo(String token) {
        Claims claims = Jwts.parserBuilder()
                // 토큰 발급자의 발급 당시의 서명을 넣어줌
                .setSigningKey(secretKey)
                // 서명 위조 검사 : 위조된 경우에는 예외가 발생
                // 위조되지 않았다면 payload를 리턴
                .build()
                .parseClaimsJws(token)
                .getBody();

        log.info("claims {}",claims);

        return TokenUserInfo.builder()
                .email(claims.getSubject())
                // 클레임이 get할 수 있는게 정해져있음 Role타입을 못꺼냄
                .role(Role.valueOf(claims.get("role", String.class)))
                .build();
    }
}
