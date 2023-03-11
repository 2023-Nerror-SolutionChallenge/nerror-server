package com.gsc.nerrorserver.api.mail.dto;

import lombok.Data;

@Data
public class MailDeleteDto {
    String id; // 삭제를 원하는 계정 id (이메일 형식)
    String username; // 삭제를 원하는 계정 이메일

    String keyword; // 삭제 요청 단어, 필터링 단어
}
