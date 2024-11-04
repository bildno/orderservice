package com.playdata.orderservice.user.service;

import com.playdata.orderservice.common.auth.TokenUserInfo;
import com.playdata.orderservice.user.dto.UserLoginReqDto;
import com.playdata.orderservice.user.dto.UserResDto;
import com.playdata.orderservice.user.dto.UserSaveReqDto;
import com.playdata.orderservice.user.entity.User;
import com.playdata.orderservice.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final PasswordEncoder encoder;
    private final UserRepository userRepository;

    public User userCreate(@Valid UserSaveReqDto dto) {

        if(userRepository.findByEmail(dto.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 이메일 입니당");
        }


        return userRepository.save(dto.toEntity(encoder));
    }

    public User login(UserLoginReqDto dto) {
        
        // 이메일로 유저 조회
        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow(() ->
                new EntityNotFoundException("업는사람")
        );
        
        // 비밀번호 확인 
        // 암호화되어있으므로 encoder에게 부탁
        if (!encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않다");
        }
        
        return user;
    }

    public UserResDto myInfo() {

        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserResDto user = userRepository.findByEmail(userInfo.getEmail()).orElseThrow(() -> new EntityNotFoundException("없는 유저")).fromEntity();


        return user;



    }

    public List<UserResDto> userList(Pageable pageable) {
        // UserResDto가 여러개 리턴
        // 페이징 처리 1페이지 요청 한 화면에 보여줄 회원 수 6명

//        Pageable pageable = PageRequest.of(0, 6); 컨트롤러에서 받아서 필요없음

        Page<User> users = userRepository.findAll(pageable);

        // 실질적 데이터(userList)
        List<User> content = users.getContent();
        List<UserResDto> dtoList = content.stream().map(user -> user.fromEntity()).collect(Collectors.toList());

        // 총 페이지 수
        users.getTotalPages();
        // 총 데이터 갯수
        users.getTotalElements();

        return dtoList;
    }
}
