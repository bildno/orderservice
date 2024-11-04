package com.playdata.orderservice.user.dto;

import com.playdata.orderservice.common.entity.Address;
import com.playdata.orderservice.user.entity.Role;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResDto {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private Address address;

}
