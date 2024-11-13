package com.playdata.orderservice.product.service;

import com.playdata.orderservice.common.Configs.AwsS3Config;
import com.playdata.orderservice.product.dto.ProductResDto;
import com.playdata.orderservice.product.dto.ProductSaveReqDto;
import com.playdata.orderservice.product.dto.ProductSearchDto;
import com.playdata.orderservice.product.entity.Product;
import com.playdata.orderservice.product.entity.QProduct;
import com.playdata.orderservice.product.repository.ProductRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.playdata.orderservice.product.entity.QProduct.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final JPAQueryFactory factory;
    private final AwsS3Config awsS3Config;

    public Product productCreate(ProductSaveReqDto dto) throws IOException {

        MultipartFile productImage = dto.getProductImage();

        String uniqueFileName = UUID.randomUUID() + "_" + productImage.getOriginalFilename();

//        File file = new File("C:\\Users\\gingu\\Desktop\\develop\\upload\\orderserviceimage\\" + uniqueFileName);
//
//        try {
//            productImage.transferTo(file);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        
        // 더 이상 로컬 경로에 이미지를 저장하지 않고, s3버킷에 저장

            String imageUrl = awsS3Config.uploadToS3Bucket(productImage.getBytes(), uniqueFileName);




        Product product = dto.toEntity();
        product.updateImagePath(imageUrl); // 파일명이 아닌 s3 오브젝트의 url이 저장될 것이다.
        return productRepository.save(product);

    }

    public Page<ProductResDto> getList(ProductSearchDto searchDto, Pageable pageable) {

//        Page<Product> products = productRepository.findAll(pageable);
//        List<Product> content = products.getContent();
//        List<ProductResDto> dtoList = content.stream().map(prod -> prod.fromEntity()).collect(Collectors.toList());

        // 클라이언트단에 페이징에 필요한 데이터를 제공하기 위해 Page 객체 자체를 넘기려고 함
        // Page 안에 Entity가 들어 있으니 이걸 dto로 변환해서 넘기고 싶음 (Page 객체는 유지)
        // map을 통해 Product를 dto로 일괄 변환해서 리턴중
        //  Page<ProductResDto> productResDtos = products.map(p -> p.fromEntity());

        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (searchDto.getSearchName() != null) {
            // 상품 이름 검색 조건
            if (searchDto.getCategory().equals("name")) {
                booleanBuilder.and(product.name.like("%" + searchDto.getSearchName() + "%"));
            // 상품 카테고리 검색 조건    
            }else if (searchDto.getCategory().equals("category")) {
                booleanBuilder.and(product.category.like("%" + searchDto.getSearchName() + "%"));
            }
        }
        
        // QueryDsl을 이용한 검색 및 페이징 처리
        List<Product> products = factory
                .selectFrom(product)
                .where(booleanBuilder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 총 검색 결과 수를 구하는 쿼리
        long total = factory
                .selectFrom(product)
                .where(booleanBuilder)
                .fetchCount();

        // QueryDSL로 조회한 내용을 모두 포함하는 Page 객체 생성
        Page<Product> productImpl = new PageImpl<>(products, pageable, total);
        // map을 통해 productImpl를 dto로 일괄 변환해서 리턴중
        Page<ProductResDto> productResDtos = productImpl.map(p -> p.fromEntity());

        return productResDtos;

    }

    public void productDelete(Long id) throws Exception {
        Product product1 = productRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Product not found")
        );
        String imagePath = product1.getImagePath();
        awsS3Config.deleteFromS3Bucket(imagePath);

        productRepository.deleteById(id);

    }
}
