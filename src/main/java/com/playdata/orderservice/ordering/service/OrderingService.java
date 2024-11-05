package com.playdata.orderservice.ordering.service;

import com.playdata.orderservice.common.auth.TokenUserInfo;
import com.playdata.orderservice.ordering.dto.OrderingListResDto;
import com.playdata.orderservice.ordering.dto.OrderingSaveReqDto;
import com.playdata.orderservice.ordering.entity.OrderDetail;
import com.playdata.orderservice.ordering.entity.OrderStatus;
import com.playdata.orderservice.ordering.entity.Ordering;
import com.playdata.orderservice.ordering.repository.OrderingRepository;
import com.playdata.orderservice.product.entity.Product;
import com.playdata.orderservice.product.repository.ProductRepository;
import com.playdata.orderservice.user.entity.User;
import com.playdata.orderservice.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderingService {

    private final OrderingRepository orderingRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;


    public Ordering createOrdering(List<OrderingSaveReqDto> dtoList, TokenUserInfo userInfo) {

        User user = userRepository.findByEmail(userInfo.getEmail()).orElseThrow(
                () -> new EntityNotFoundException("User Not Found"));


        // Ordering(주문) 객체 생성
        Ordering ordering = Ordering.builder()
                .user(user)
                .orderDetails(new ArrayList<>()) // 아직 주문 상세 들어가기 전
                .build();

        // 주문 상세 내역에 대한 처리를 반복문으로 지정
        for (OrderingSaveReqDto dto : dtoList) {
            
            // dto에는 상품 고유아이디가 존재 그걸 사용해서 product 객체 조회
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(
                    () -> new EntityNotFoundException("Product Not Found")
            );

            // 상품의 재고 확인
            int quantity = dto.getProductCount();
            if(product.getStockQuantity() < quantity) {
                throw new IllegalArgumentException("재고 부족");
            }

            // 재고가 부족하지 않다면 주문수량만큼 빼주자
            product.updateQuantity(quantity);

            // 주문 상세 내역 엔터티를 생성
            OrderDetail orderDetail = OrderDetail.builder()
                    .product(product)
                    .ordering(ordering)
                    .quantity(quantity)
                    .build();

            // 주문 내역 리스트에 상세 내역을 add
            // (cascadeType.PERSIST로 세팅했기 때문에 함께 ADD가 진행됨)
            ordering.getOrderDetails().add(orderDetail);
        }

        // Ordering 객체를 save하면 내부에 있는 detail 리스트도 함께 insert가 진행됨
        return orderingRepository.save(ordering);
    }

    public List<OrderingListResDto> findAll() {
        List<Ordering> orderingList = orderingRepository.findAll();

        List<OrderingListResDto> dtoList = orderingList.stream().map(order -> order.fromEntity()).collect(Collectors.toList());

        return dtoList;
    }

    public List<OrderingListResDto> myOrders(TokenUserInfo userInfo) {
        /*
         OrderingListResDto -> OrderDetailDto(static 내부 클래스)
         {
            id: 주문번호,
            userEmail: 주문한 사람 이메일,
            orderStatus: 주문 상태
            orderDetails: [
                {
                    id: 주문상세번호,
                    productName: 상품명,
                    count: 수량
                },
                {
                    id: 주문상세번호,
                    productName: 상품명,
                    count: 수량
                },
                {
                    id: 주문상세번호,
                    productName: 상품명,
                    count: 수량
                }
                ...
            ]
         }
         */

        User user = userRepository.findByEmail(userInfo.getEmail()).orElseThrow( () -> new EntityNotFoundException("User Not Found"));

        List<Ordering> orderingList = orderingRepository.findByUser(user);
        
        //ordering 엔터티를 DTO로 변환 주문 상세에 대한 변환도 필요
        List<OrderingListResDto> collect = orderingList.stream().map(Ordering::fromEntity).collect(Collectors.toList());


        return collect;

    }

    public Ordering orderCancel(Long id) {
        Ordering order = orderingRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Order Not Found"));
        order.updateStatus(OrderStatus.CANCELED); // 더티체킹(save를 하지 않아도 변경 감지)

        return order;
    }
}
