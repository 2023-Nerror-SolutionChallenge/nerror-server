package com.gsc.nerrorserver.api.mail.dto;

import lombok.Data;

@Data
public class MailSaveDto {

    String id; // 저장을 원하는 Marbon 계정
    String username; // 저장을 원하는 이메일 계정
}
