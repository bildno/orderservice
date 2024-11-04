package com.playdata.orderservice.product.repository;

import com.playdata.orderservice.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {



}
