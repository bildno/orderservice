package com.playdata.orderservice.common.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter 
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable // 타 엔터티에서 사용 가능한 영태로 만드는 아노테이션
public class Address {


    private String city;
    private String street;
    private String zipCode;


}
