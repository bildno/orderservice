package com.playdata.orderservice.user.dto;

import lombok.*;

@Setter @Getter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginReqDto {
    private String email;
    private String password;


}
