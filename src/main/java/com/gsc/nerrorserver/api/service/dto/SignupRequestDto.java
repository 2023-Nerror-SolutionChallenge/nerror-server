package com.gsc.nerrorserver.api.service.dto;

import lombok.Data;

@Data
public class SignupRequestDto {

    String id; // 이메일
    String password;
    String nickname;
    String picture;
}
