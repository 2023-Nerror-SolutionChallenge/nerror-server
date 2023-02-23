package com.gsc.nerrorserver.api.service.dto;

import lombok.Data;

@Data
public class SignupRequestDto {

    String password;
    String nickname;
    String picture;
}
