package com.playdata.orderservice.ordering.entity;

import com.playdata.orderservice.ordering.dto.OrderingListResDto;
import com.playdata.orderservice.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
public class OrderDetail {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordering_id")
    private Ordering ordering;


    // 엔터티를 dto로 변환하는 메서드
    // 내부 클래스이기 때문에 orderingListResDto 이름으로 참조
    public OrderingListResDto.OrderDetailDto fromEntity(){

        return OrderingListResDto.OrderDetailDto.builder()
                .id(id)
                .productName(product.getName())
                .count(quantity)
                .build();
    }
}
