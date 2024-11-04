package com.playdata.orderservice.product.service;

import com.playdata.orderservice.product.dto.ProductResDto;
import com.playdata.orderservice.product.dto.ProductSaveReqDto;
import com.playdata.orderservice.product.entity.Product;
import com.playdata.orderservice.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public Product productCreate(ProductSaveReqDto dto) {

        MultipartFile productImage = dto.getProductImage();

        String uniqueFileName = UUID.randomUUID() + "_" + productImage.getOriginalFilename();

        File file = new File("C:\\Users\\gingu\\Desktop\\develop\\upload\\orderserviceimage\\" + uniqueFileName);

        try {
            productImage.transferTo(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Product product = dto.toEntity();
        product.updateImagePath(uniqueFileName);
        return productRepository.save(product);

    }

    public List<ProductResDto> getList(Pageable pageable) {

        Page<Product> products = productRepository.findAll(pageable);
        List<Product> content = products.getContent();
        List<ProductResDto> dtoList = content.stream().map(prod -> prod.fromEntity()).collect(Collectors.toList());

        return dtoList;

    }
}
