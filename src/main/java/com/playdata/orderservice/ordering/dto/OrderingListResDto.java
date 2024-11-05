package com.playdata.orderservice.ordering.dto;

import com.playdata.orderservice.ordering.entity.OrderStatus;
import lombok.*;

import java.util.List;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderingListResDto {


    private Long id;
    private String userEmail;
    private OrderStatus orderStatus;
    private List<OrderDetailDto> orderDetails;

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderDetailDto{
        private Long id;
        private String productName;
        private int count;

    }
}



