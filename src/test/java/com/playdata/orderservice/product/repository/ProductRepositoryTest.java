package com.playdata.orderservice.product.repository;

import com.playdata.orderservice.product.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class ProductRepositoryTest {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Test
    @DisplayName("상품 마니 넣기")
    void bulkInsert() {
        for (int i = 1; i <=100; i++) {
            Product p = Product.builder()
                    .name("더미상품" + i)
                    .price(i + 1000)
                    .category("더미 카테고리" + i)
                    .stockQuantity(i)
                    .imagePath("")
                    .build();
            productRepository.save(p);
        }



        
        // then
    }
    

}