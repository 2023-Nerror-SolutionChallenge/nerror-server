package com.gsc.nerrorserver.api.mail.dto;

import lombok.Data;

@Data
public class MailDeleteDto {
    String id;
    String username;
    String password;
    String host;
    String port;

    String keyword; // 삭제 요청 단어
}
