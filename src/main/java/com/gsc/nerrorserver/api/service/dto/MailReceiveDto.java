package com.gsc.nerrorserver.api.service.dto;

import com.google.errorprone.annotations.Keep;
import lombok.Data;

@Data
public class MailReceiveDto {

    String id;
    String username;
    String password;
    String host;
    String port;

}
