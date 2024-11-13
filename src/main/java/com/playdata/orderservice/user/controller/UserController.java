package com.playdata.orderservice.user.controller;

import com.playdata.orderservice.common.auth.JwtTokenProvider;
import com.playdata.orderservice.common.dto.CommonErrorDto;
import com.playdata.orderservice.common.dto.CommonResDto;
import com.playdata.orderservice.user.dto.UserLoginReqDto;
import com.playdata.orderservice.user.dto.UserResDto;
import com.playdata.orderservice.user.dto.UserSaveReqDto;
import com.playdata.orderservice.user.entity.User;
import com.playdata.orderservice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
@EnableMethodSecurity // 권한 검사를 controller의 메서드에서 전역적으로 수행하기 위한 설정
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Qualifier("user-template") // RedisTemplate이 여러개 빈 등록되었을 경우 명시
    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/create")
    public ResponseEntity<?> userCreate(@Valid @RequestBody UserSaveReqDto dto) {
        User user = userService.userCreate(dto);

        CommonResDto resDto = new CommonResDto(HttpStatus.CREATED, "member create 성공", user.getId());
        return new ResponseEntity<>(resDto, HttpStatus.CREATED);

    }


    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody UserLoginReqDto dto){
        User user = userService.login(dto);

        // 회원정보가 일치한다면, JWT를 클라이언트에게 발급해 주어야 한다. -> 로그인 유지를 위해
        // access token을 생성해 발급 -> 수명이 짧음
        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole().toString());

        // refresh token 생성
        // access token의 수명이 만료되었을 경우 refresh token을 확인해 유효한 경우
        // 사용자에게 로그인 없이 access token을 재발급 해 줄 용도

        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getRole().toString());
        
        // refresh Token 을 db에 저장 -> redis에 저장
        redisTemplate.opsForValue().set(user.getEmail(), refreshToken,240, TimeUnit.HOURS);

        // 생성된 토큰외에 추가로 전달하고싶은 데이터가 있다면 
        // 맵을 사용하는 것이 편함
        Map<String, Object> logInfo = new HashMap<>();
        logInfo.put("token", token);
        logInfo.put("id", user.getId());

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "로그인 성공", logInfo);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    // 회원정보 조회 (관리자) -> ADMIN만 회원 목록 전체 조회 가능
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    // 컨트롤러 파라미터로 pageable 선언을 해놓으면 페이징 파라미터 처리를 쉽게 진행 할 수 있음
    // /list/number=1&size=10&sort=name,desc 이러케
    // 요청 시 쿼리스트링이 전달되지 않으면 기본값 0, 20 unsorted
    public ResponseEntity<?> userList(Pageable pageable) {
        log.info("/user/list : get!!");
        log.info("pageable {}", pageable);

        List<UserResDto> dtoList = userService.userList(pageable);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "userList 조회 성공", dtoList);


        return ResponseEntity.ok().body(resDto);
    }
    
    // 회원 정보 조회 (마이페이지) - 일반회원의 요청
    @GetMapping("/myinfo")
    public ResponseEntity<?> myInfo() {
        UserResDto dto = userService.myInfo();

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "myInfo 조회 성공", dto);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }
    
    // access token이 만료되어 새 토큰을 요청
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<Object, String> id) {

        User user = userService.findbyId(Long.parseLong(id.get("id")));

        // email로 redis를 조회해서 refresh token을 가져오자
        Object obj = redisTemplate.opsForValue().get(user.getEmail());

        if(obj == null){ // null 이라면 refresh 토큰의 수명이 다 된것
            return new ResponseEntity<>(new CommonErrorDto(
                    HttpStatus.UNAUTHORIZED, "EXPIRED_RT"
                    ), HttpStatus.UNAUTHORIZED);
        }

        // 새로운 access token을 발급
        String newAccessToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole().toString());

        Map<String, Object> info = new HashMap<>();
        info.put("token", newAccessToken);
        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "새 토큰 발급됨", info);


        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

}
