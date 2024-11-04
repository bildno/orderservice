package com.playdata.orderservice.product.dto;


import com.playdata.orderservice.product.entity.Product;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResDto {

    // ProductResDto(id, name, category, price, stockQuantity, imagePath)

    private Long id;
    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;
    private String imagePath;




}
