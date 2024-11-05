package com.playdata.orderservice.ordering.controller;


import com.playdata.orderservice.common.auth.TokenUserInfo;
import com.playdata.orderservice.common.dto.CommonResDto;
import com.playdata.orderservice.ordering.dto.OrderingListResDto;
import com.playdata.orderservice.ordering.dto.OrderingSaveReqDto;
import com.playdata.orderservice.ordering.entity.Ordering;
import com.playdata.orderservice.ordering.service.OrderingService;
import com.playdata.orderservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
@EnableMethodSecurity
public class OrderingController {

    private final OrderingService orderingService;


    @PostMapping("/create")
    public ResponseEntity<?> orderingCreate(
            // 전역 인증 정보를 담아놓는 ContextHolder에서 메서드 호출 시에
            // 사용자 인증 정보를 전달해 달라는 아노테이션
            @AuthenticationPrincipal TokenUserInfo userInfo,
            @RequestBody List<OrderingSaveReqDto> dtoList) {


        Ordering ordering = orderingService.createOrdering(dtoList, userInfo);
        CommonResDto resDto = new CommonResDto(HttpStatus.CREATED, "정상 주문 완료", ordering.getId());


        return new ResponseEntity<>(resDto, HttpStatus.CREATED);
    }

    // 내 주문만 볼 수 있는 myOrders
    @GetMapping("/my-order")
    public ResponseEntity<?> myOrder(@AuthenticationPrincipal TokenUserInfo userInfo) {

        List<OrderingListResDto> list = orderingService.myOrders(userInfo);

        new CommonResDto(HttpStatus.OK, "조회완료", list);

        return new ResponseEntity<>(list, HttpStatus.OK);



    }

    // 전체 주문 조회
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> list() {

        List<OrderingListResDto> all = orderingService.findAll();
        new CommonResDto(HttpStatus.OK, "조회완료", all);

    return new ResponseEntity<>(all, HttpStatus.OK);
    }


    // 주문 상태를 취소하는 요청 (ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> orderCancel(@PathVariable Long id) {
        Ordering ordering = orderingService.orderCancel(id);
        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "변경완료", ordering.getId());

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

}
