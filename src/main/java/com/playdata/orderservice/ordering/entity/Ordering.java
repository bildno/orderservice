package com.playdata.orderservice.ordering.entity;

import com.playdata.orderservice.ordering.dto.OrderingListResDto;
import com.playdata.orderservice.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Ordering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    @OneToMany(mappedBy = "ordering", cascade = CascadeType.PERSIST)
    private List<OrderDetail> orderDetails;


    public OrderingListResDto fromEntity() {
        List<OrderDetail> orderDetailList = this.getOrderDetails();
        List<OrderingListResDto.OrderDetailDto> orderDetailDtos = new ArrayList<>();

        //OrderDetail 엔터티를 OrderDetailDto로 변환
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetailDtos.add(orderDetail.fromEntity());
        }



        // 주문 상세 내역 dto 포장이 완료되면 하나의 주문 내역 자체를 dto로 변환해 리턴
        return OrderingListResDto.builder()
                .id(this.id)
                .userEmail(this.user.getEmail())
                .orderStatus(this.orderStatus)
                .orderDetails(orderDetailDtos)
                .build();
    }

    // status 세터
    public void updateStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
