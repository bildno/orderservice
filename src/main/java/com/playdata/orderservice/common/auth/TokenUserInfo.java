package com.playdata.orderservice.common.auth;

import com.playdata.orderservice.user.entity.Role;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenUserInfo {

    private String email;
    private Role role;

}
