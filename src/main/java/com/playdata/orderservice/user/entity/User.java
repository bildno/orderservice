package com.playdata.orderservice.user.entity;

import com.playdata.orderservice.common.entity.Address;
import com.playdata.orderservice.user.dto.UserResDto;
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
@Table(name = "tbl_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(length = 20, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Embedded // @Embeddable로 선언된 값 대입 (기본 생성자 필수)
    private Address address;
    
    @Enumerated(EnumType.STRING) // 데이터베이스에 저장할 때 상수 이름을 문자화해서 저장
    @Builder.Default // Builder시 초기화된 값으로 세팅하기 위한 아노테이션
    private Role role = Role.USER;

    public UserResDto fromEntity() {
        return UserResDto.builder()
                .id(id)
                .name(name)
                .email(email)
                .address(address)
                .role(role)
                .build();
    }

}
