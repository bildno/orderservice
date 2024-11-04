package com.playdata.orderservice.product.controller;

import com.playdata.orderservice.common.dto.CommonResDto;
import com.playdata.orderservice.product.dto.ProductResDto;
import com.playdata.orderservice.product.dto.ProductSaveReqDto;
import com.playdata.orderservice.product.entity.Product;
import com.playdata.orderservice.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    // 요청과 함께 이미지가 전달이 됨, 해당 이미지를 처리하는 방식이 두 가지로 나뉨
    // 1. js의 FormData 객체를 통해 모든 데이터를 전달 (multipart/form-data 형식으로 전달, form 태그 x)
    // 2. JSON 형태로 전달(이미지를 Base64 인코딩을 통해 문자열로 변환해서 전달)
    // ModelAttribute를 사용해서 FormData 객체를 우리가 평소에 사용하는 form 태그 방식으로 받겠다.
    // Model 기능을 쓰는것은 아님 (React 단에는 model을 전달할 수 없음)
    public ResponseEntity<?> create(ProductSaveReqDto dto){

        log.info("/product/create : POST");
        Product product = productService.productCreate(dto);

        CommonResDto resDto = new CommonResDto(HttpStatus.CREATED, "product 등록 성공", product.getId());

        return new ResponseEntity<> (resDto, HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(Pageable pageable){
        log.info("/product/list : GET");

        // 페이징이 필요합니다. 리턴은 ProductResDto 형태로 리턴됩니다.
        // ProductResDto(id, name, category, price, stockQuantity, imagePath)

        List<ProductResDto> list = productService.getList(pageable);

        new CommonResDto(HttpStatus.OK, "조회성공", list);

        return new ResponseEntity<> (list, HttpStatus.CREATED);
    }


}
