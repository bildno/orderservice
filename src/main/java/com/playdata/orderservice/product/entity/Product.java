package com.playdata.orderservice.product.entity;

import com.playdata.orderservice.common.entity.BaseTimeEntity;
import com.playdata.orderservice.product.dto.ProductResDto;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tbl_product")
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;
    private String imagePath;

    // 이미지 경로 setter 직접 생성
    public void updateImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public ProductResDto fromEntity() {
        return ProductResDto.builder()
                .id(id)
                .name(name)
                .category(category)
                .price(price)
                .stockQuantity(stockQuantity)
                .imagePath(imagePath)
                .build();
    }


}